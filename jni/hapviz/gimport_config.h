#ifndef GIMPORT_CONFIG_H
#define GIMPORT_CONFIG_H

#include "gfilter.h"
#include "gfilter_cflow.h"

std::vector<GFilter *> CImport::inputfilters;

unsigned int CImport::initInputfilters()
{
	inputfilters.push_back(new GFilter_cflow4);

	return inputfilters.size();
}

#endif /* GIMPORT_CONFIG_H */
