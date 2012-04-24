#ifndef GFILTER_H
#define GFILTER_H

/**
 *	\file gfilter.h
 *	\brief Filter to im- and export various file formats
 *
 * 	This file is subject to the terms and conditions defined in
 * 	files 'BSD.txt' and 'GPL.txt'. For a list of authors see file 'AUTHORS'.
 */

#include <string>
#include <stdexcept>

#include "cflow.h"
#include "gutil.h"
#include "HashMapE.h"

/**
 *	\class	GFilter
 *	\brief	GFilter is an pure virtual class which can be implemented by various in-/outputfilter
 */
class GFilter {
	public:
		GFilter(std::string formatName = "insert name here", std::string humanReadablePattern = "insert extensionname here",
		      std::string regexPattern = "nomatch^");
		virtual ~GFilter(){};
		// general methods
		std::string getFormatName() const;
		std::string getHumanReadablePattern() const;
		virtual bool acceptFilename(std::string in_filename) const;

		// import methods
		virtual void read_stream(std::istream & in_stream, CFlowList & flowlist, const IPv6_addr & local_net, const IPv6_addr & netmask, unsigned int flowcount, bool append) const=0;
		virtual bool acceptFileForReading(std::string in_filename) const=0;

		// export methods
		virtual bool acceptFileForWriting(std::string in_filename) const;
		virtual void write_stream(const std::ostream & out_stream, const Subflowlist subflowlist, bool appendIfExisting = true) const;

	protected:
		typedef HashKeyIPv6_5T flowHashKey;
		typedef hash_map<HashKeyIPv6_5T, cflow_t *, HashFunction<HashKeyIPv6_5T> , HashFunction<HashKeyIPv6_5T> > flowHashMap;

		std::string formatName; ///< Name of this format (e.g. pcap, cflow, nfdump)
		std::string humanReadablePattern; ///< A human "readable" pattern for the fileextension (e.g. *.pcap)
		std::string regexPattern; ///< A regex pattern for the file extension (e.g. .*\\.pcap$)
};

#endif /* GFILTER_H */
