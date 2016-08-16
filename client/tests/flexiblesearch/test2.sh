#!/bin/sh
curl "https://localhost:9002/tools/flexiblesearch/execute?itemtype=Product&fields=code,name" --user admin:nimda -k --basic > results/result2
