#!/bin/sh
curl "https://localhost:9002/tools/flexiblesearch/execute?query=select%20\{pk\}%20from%20\{Product\}&fields=code,name" --user admin:nimda -k --basic > results/result1
