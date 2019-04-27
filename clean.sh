cd model_dir 
rm -rf ./*
cd ..
bk=`date "+%Y%m%d-%H:%M:%S"`
mv result.csv ./backedup_result/${bk}.csv
rm -rf out_bball 
rm -rf *.csv
rm -rf *.tmp
rm -rf phv_bash.sh
echo "clean ok"

