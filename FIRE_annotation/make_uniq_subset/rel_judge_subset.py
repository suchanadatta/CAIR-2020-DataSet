# Usage : python rel_judge_subset.py <qrel_file_path> <result_file_path>
# qrelFile = 4-column TREC format qrel file and column headers should be specified
# resFile = 6-column TREC format file with column headers specified

import csv
import sys
import pandas as pd

qrelpath = sys.argv[1]
resfilepath = sys.argv[2]

read_res_file = csv.reader(open(resfilepath, "r"), delimiter="\t")
outfile = open("/store/FIRE_2020_task_proposal/chuan-an-lin.res/chuan_post_event_top_100.judge", "w")
out_diff = open("/store/FIRE_2020_task_proposal/chuan-an-lin.res/chuan_post_event_diff.judge", "w")

outfile.write('qid\tq0\tdocid\n')
qid = ""
count = 0
i = 1
for row in read_res_file:
    # print(row)
    if qid == "" or row[0] == qid:
        if count < 100:
            qid = row[0]
            outfile.write(row[0])
            outfile.write('\t')
            outfile.write(row[1])
            outfile.write('\t')
            outfile.write(row[2])
            outfile.write('\n')
            count = count + 1
            i = i + 1
    else:
        qid = ""
        count = 1
        i = 1
        outfile.write(row[0])
        outfile.write('\t')
        outfile.write(row[1])
        outfile.write('\t')
        outfile.write(row[2])
        outfile.write('\n')
        i = i + 1
outfile.close()

df_qrel = pd.read_csv(qrelpath)
# print(df_qrel.head())

df_res = pd.read_csv("/store/FIRE_2020_task_proposal/chuan-an-lin.res/chuan_post_event_top_100.judge")
# print(df_res.head())

# df = pd.read_csv('some_data.csv', usecols = ['col1','col2'], low_memory = True)

df_diff = pd.merge(df_qrel, df_res, how='outer', indicator='Exist')
df_diff = df_diff.loc[df_diff['Exist'] == 'right_only']
# print(df_diff)
out_diff.write(df_diff.to_csv(index=False))




