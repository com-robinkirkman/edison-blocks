#!/bin/bash
LD_LIBRARY_PATH=/usr/local/lib java -cp /usr/local/lib/java/*:target/blocks.jar org.esialb.edison.blocks.Main "$@"
