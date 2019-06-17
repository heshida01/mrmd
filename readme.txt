MRMD：
有两个版本（可从我们的网站或者在这里的mrmmjar目录里下载）：
mrmd_7.jar（jdk1.7）
mrmd_8.jar  (jdk1.8)

java -jar mrmd_7.jar -i input -o output.txt  或者
    java -jar mrmd_8.jar -i input -o output.txt  
-i inputFile: 输入文件的绝对路径,目前只支持.arff文件格式。
-o outputFile: 特征选择的具体特征打分和排序信息。

可选参数
（1）距离函数 -df ：
the distance function default(1)，默认为1 
(1 = Euclidean Distance, 2 = Cosine Distance, 3 = Tanimoto coefficient, 4 = mean)
（2）经过特征选择后的arff文件 -a ：
outputfile of arff default (out.arff)
（3）用于自动验证特征准确率的分类器 -m ：
opt model type defauly(rf) 
（目前可支持： rf, svm, bagging，其中rf表示randomforest，
（4）如果特征维度比较多的时候，可以只挑选部分特征进行自动化特征选择 -sn 1000
（5） -N 表示不进行优化

