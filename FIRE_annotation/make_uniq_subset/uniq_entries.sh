#!/bin/bash

# use the following command to merge all result files with unique entries
# input - all result files (6-col TREc format)
# output - 3 column file (qid, q0, docid)

echo "qid	q0	docid" >> /store/causalIR/fire_2021/results_2021/total_uniq.tsv
cat /store/causalIR/fire_2021/results_2021/galway_submission_semantic_ir.tsv /store/causalIR/fire_2021/results_2021/CNLP-NITS_Run_Query_Narrative.tsv /store/causalIR/fire_2021/results_2021/CNLP-NITS_Run_Query_Title.tsv | sort -k1,1n | awk '{print $1"\t"$2"\t"$3}' | uniq >> /store/causalIR/fire_2021/results_2021/total_uniq.tsv
