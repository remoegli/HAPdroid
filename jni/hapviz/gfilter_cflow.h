/**
 *	\file gfilter_cflow.h
 *	\brief Filter to import and export cflow files
 *
 * 	This file is subject to the terms and conditions defined in
 * 	files 'BSD.txt' and 'GPL.txt'. For a list of authors see file 'AUTHORS'.
 */

#ifndef GFILTER_CFLOW_H_
#define GFILTER_CFLOW_H_

#include <iosfwd>
#include <string>
#include <stdint.h>
#include <boost/iostreams/filtering_streambuf.hpp>
#include <boost/iostreams/filtering_stream.hpp>
#include <boost/iostreams/filter/gzip.hpp>
#include <boost/iostreams/device/file.hpp>

#include "gfilter.h"
#include "IPv6_addr.h"
#include "cflow.h"

class GFilter_cflow4: public GFilter {
public:
	GFilter_cflow4(std::string formatName = "cflow4", std::string humanReadablePattern = "*.gz", std::string regexPattern = ".*\\.gz$");

	// import methods
	virtual bool acceptFileForReading(std::string in_filename) const;
	virtual void read_stream(std::istream & in_stream, CFlowList & flowlist, const IPv6_addr & local_net, const IPv6_addr & netmask, unsigned int flowcount, bool append) const;
	void read_stream(std::istream & in_stream, CFlowList & flowlist, unsigned int flowcount, bool append = false) const;

	// export methods
	virtual void write_flow(boost::iostreams::filtering_ostream & out_filestream, const cflow_t & cf) const;

protected:
	// export methods
	virtual void openGzipStream(boost::iostreams::filtering_ostream & out_filestream, boost::iostreams::file_sink & out_filesink,
	      const std::string & in_filename) const;

	// import methods
	void openGunzipStream(boost::iostreams::filtering_istream & in_filestream, boost::iostreams::file_source & infs, const std::string & in_filename) const;
	uint32_t getUncompressedFileSize(std::ifstream & in_filestream) const;
	virtual bool checkCflowFileSize(uint32_t size) const;
	virtual unsigned int getNumberOfFlows(uint32_t size) const;
	virtual void read_flow(cflow4 & cflow4, CFlowList & flowlist) const;
	virtual void read_flow(cflow4 & cflow4, CFlowList & flowlist, uint8_t & oldmagic) const;
};

#endif /* GFILTER_CFLOW_H_ */
