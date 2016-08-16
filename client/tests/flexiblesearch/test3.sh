#!/bin/bash
curl "https://localhost:9002/tools/flexiblesearch/execute?query=select%20\{pk\}%20from%20\{Product\}%20where%20\{code\}%20=1776948&fields=code,supercategories&debug=true&ref=Category:code,supercategories" --user admin:nimda -k
