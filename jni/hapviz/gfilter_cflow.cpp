/**
 *	\file gfilter_cflow.cpp
 *	\brief Filter to import and export cflow files
 *
 * 	This file is subject to the terms and conditions defined in
 * 	files 'BSD.txt' and 'GPL.txt'. For a list of authors see file 'AUTHORS'.
 */

#include <iostream>
#include <sstream>
#include <boost/iostreams/filtering_streambuf.hpp>
#include <boost/iostreams/filtering_stream.hpp>
#include <boost/iostreams/filter/gzip.hpp>
#include <boost/iostreams/device/file.hpp>

#include "gfilter_cflow.h"
#include "gutil.h"
#include "cflow.h"

using namespace std;

/**
 *	\class	GFilter_cflow4
 *	\brief	Class to import old cflow4 files
 *
 *	\param	formatName	Name of this format
 *	\param	humanReadablePattern	A simple name pattern for files of this type, used by e.g. the GUI
 *	\param	regexPattern	Regex pattern used internally
 */
GFilter_cflow4::GFilter_cflow4(std::string formatName,
		std::string humanReadablePattern, std::string regexPattern) :
		GFilter(formatName, humanReadablePattern, regexPattern) {
	// nothing to do here
}

/**
 *	Returns the number of bytes the uncompressed gz-file contains
 *
 *	\param in_filename Filestream for which the size gets looked up
 *
 *	\return Number of bytes
 */
uint32_t GFilter_cflow4::getUncompressedFileSize(
		ifstream & in_filestream) const {
	uint32_t isize = 0; // By default, GZIP can just store up to 4GiBytes of data
	long old_pos = in_filestream.tellg();

	in_filestream.seekg(-4, ios::end);

	// read the (uncompressed) filesize
	in_filestream.read((char *) &isize, 4); // FIXME: big endian will freak out here
	in_filestream.seekg(old_pos);
	return isize;
}

/**
 *	Creates a stream which uncompresses a gz-file
 *
 *	\param in_filestream The resulting, uncompressed stream
 *	\param in_filename Filename of the compressed file
 *
 *	\exception std::string Errortext
 */
void GFilter_cflow4::openGunzipStream(
		boost::iostreams::filtering_istream & in_filestream,
		boost::iostreams::file_source & infs,
		const string & in_filename) const {
	// Add stream decompressor
	in_filestream.push(boost::iostreams::gzip_decompressor());

	// Open input file and link it to stream chain
	if (!infs.is_open()) {
		string errtext = "ERROR: could not open file source \"" + in_filename
				+ "\".";
		throw errtext;
	}
	in_filestream.push(infs);
	cout << "Reading file " << in_filename << ":\n";
}

/**
 *	Reads a given file into a given flowlist.
 *
 *	\param filename Filename of the compressed cflow_t file
 *	\param flowlist List which will be filled with the cflows
 *	\param local_net Contains the IP
 *	\param netmask Contains the netmask
 *	\param append If true, do not clear the flowlist, instead append it to the existing data (not yet used)
 */
void GFilter_cflow4::read_stream(std::istream & in_stream, CFlowList & flowlist,
		const IPv6_addr & local_net, const IPv6_addr & netmask,
		unsigned int flowcount, bool append) const {
	read_stream(in_stream, flowlist, flowcount, append);
	return;
}

/**
 *	Reads a given file into a given flowlist.
 *
 *	\param in_stream istream of the uncompressed cflow_t file
 *	\param flowlist List which will be filled with the cflows
 *	\param append If true, do not clear the flowlist, instead append it to the existing data (not yet used)
 *
 *	\exception std::string Errortext
 */
void GFilter_cflow4::read_stream(std::istream & in_stream, CFlowList & flowlist,
		unsigned int flowcount, bool append) const {
	// TODO: take care about the append flag

	// Resize flowlist to fit for all cflows
	flowlist.resize(flowcount);

	cflow4 tmpCflow4;
	// Read file data: get all flows
	cout << "starting to read flows\n";
	for (unsigned int i = 0; i < flowcount; ++i) {
		in_stream.read((char *) &tmpCflow4, sizeof(struct cflow4));

		streamsize num_read = in_stream.gcount();
		if (num_read != sizeof(struct cflow4)) {
			string errtext = "ERROR: read ";
			errtext += num_read;
			errtext += " byte instead of ";
			errtext += sizeof(struct cflow4);
			errtext += ". Possibly incomplete flow read from file.";
			throw errtext;
		}

		try {
			read_flow(tmpCflow4, flowlist);
		} catch (string & error) {
			throw error;
		}
	}
	cout << "finished to read flows\n";

	// tellg() does not work on boost::iostreams::filtering_istream, so we have to work around
	char tmpChar = 'X';
	in_stream.read(&tmpChar, 1);

	// if this is still good, something went wrong
	if (in_stream.good()) {
		string error = "ERROR: flow list overflow. ";
		throw error;
	}
}

/**
 * Writes a single flow to filtering_ostream
 *
 * @param out_filestream Filename to write
 * @param cf flow to write
 *
 * @exception std::string Errortext
 */
void GFilter_cflow4::write_flow(
		boost::iostreams::filtering_ostream & out_filestream,
		const cflow_t & cf) const {
	throw "This filter does not support writing";
}

/**
 *	Creates a stream which compresses to a gz-file
 *
 *	@param in_filestream The resulting, uncompressed stream
 *	@param in_filename Filename of the compressed file
 *
 *	@exception std::string Errortext
 */
void GFilter_cflow4::openGzipStream(
		boost::iostreams::filtering_ostream & out_filestream,
		boost::iostreams::file_sink & out_filesink,
		const std::string & in_filename) const {
	throw "This filter does not support writing";
}

/**
 *	Decides if this file contains cflow4 flows
 *
 *	\param in_filename File which should be tested
 *
 *	\return True if this file contains cflow4 flows, false if not
 */
bool GFilter_cflow4::acceptFileForReading(string in_filename) const {
	if (GFilter::acceptFilename(in_filename)) {
		return true;
	}
	return false;
}

/**
 *	Decides, if the given number of bytes is a multiple of the size of a cflow4 struct
 *
 *	\param uncompressed_size Size in bytes of the whole e.g. file
 *
 *	\return True if the given number is a multiple of sizeof(cflow4)
 */
bool GFilter_cflow4::checkCflowFileSize(uint32_t uncompressed_size) const {
	if (uncompressed_size % sizeof(cflow4) != 0) {
		string errtext =
				"\nERROR: input file data size is not a multiple of a cflow4 record.\n";
		errtext +=
				"Possibly this is not a gzipped file containing cflow4 data.\n";
		stringstream ss;
		ss << "File size is: " << uncompressed_size
				<< ", sizeof(struct cflow4) is: " << sizeof(struct cflow4)
				<< "\n";
		errtext += ss.str();
		cerr << errtext;
		return false;
	}
	return true;
}

/**
 *	Return the number of flows which can be stored in a given filesize
 *
 *	\param size Size in bytes of the whole e.g. file
 *
 *	\return Number of cflow4 version flows fit into this size
 */
unsigned int GFilter_cflow4::getNumberOfFlows(uint32_t size) const {
	return size / (sizeof(struct cflow4));
}

/**
 *	Reads a single flow and stores it into a cflow_t struct
 *
 *	\param infs Inputstream to read from
 *	\param cf Cflow struct to write to
 *	\param oldmagic stores the old magic number before it got updated (assumes that all magic numbers are the same in single file)
 */
void GFilter_cflow4::read_flow(cflow4 & cflow4, CFlowList & flowlist,
		uint8_t & oldmagic) const {
	oldmagic = CFLOW_4_MAGIC_NUMBER;
	read_flow(cflow4, flowlist);
}

/**
 *	Reads a single flow and stores it into a cflow_t struct
 *
 *	\param infs Inputstream to read from
 *	\param cf Cflow struct to write to
 *
 *	\exception std::string Errortext
 */
void GFilter_cflow4::read_flow(cflow4 & tmpCflow4, CFlowList & flowlist) const {
	cflow_t cf;
	// Check flow data
	if (tmpCflow4.magic != CFLOW_4_MAGIC_NUMBER) {
		string errtext =
				"ERROR: file check failed (wrong magic number) in in CImport::read_flow4.";
		throw errtext;
	}

	cf.localIP = IPv6_addr(tmpCflow4.localIP);
	cf.localPort = tmpCflow4.localPort;
	cf.remoteIP = IPv6_addr(tmpCflow4.remoteIP);
	cf.remotePort = tmpCflow4.remotePort;
	cf.prot = tmpCflow4.prot;
	cf.flowtype = tmpCflow4.flowtype;
	cf.startMs = tmpCflow4.startMs;
	cf.durationMs = tmpCflow4.durationMs;
	cf.dOctets = tmpCflow4.dOctets;
	cf.dPkts = tmpCflow4.dPkts;
	cf.localAS = tmpCflow4.AS.local;
	cf.remoteAS = tmpCflow4.AS.remote;
	cf.tos_flags = tmpCflow4.tos_flags;
	cf.magic = CFLOW_CURRENT_MAGIC_NUMBER; // Update it to the current version

	flowlist.push_back(cf);
}
