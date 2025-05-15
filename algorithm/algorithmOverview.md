# 算法概述

## 

## <span style="color:red">**鼾声识别**</span>

基于**卷积神经网络**（CNN）构建鼾声识别的算法模型，使用tensorflow学习框架，语言Python。



### （1）数据集

收集两个公开数据集进行对模型的训练和性能验证。

+ [Snoring Dataset](https://www.kaggle.com/datasets/tareqkhanemu/snoring/data):作为一个训练集，该数据集包含鼾声和非鼾声样本各500个，是一个二分类数据集。

+ [ESC-50](https://github.com/karolpiczak/ESC-50)：作为一个验证集，该数据集含有5大类，50小类，是一个50分类数据集。



## （2）特征处理与CNN模型的创建

对每段音频进行短时傅里叶变换，依此作为模型的输入部分。构建两层卷积。

参考：[Deep-learning Snoring Detector - version 2.0](https://www.kaggle.com/code/orannahum/deep-learning-snoring-detector-version-2-0)(author：oran nahum)

主要做出改进：（1） 将短时傅里叶变换融入到CNN模型中，使之作为单独一层，即模型的输入为一维的音频数据，不在为二维的频谱图。（短时傅里叶变换在模型内部完成）

                            （2） 模型加入<span style="color:blue">**池化层**</span>，这将极大的减少模型参数数量，在<span style="color:blue">移动端部署中十分重要。</span>

                            （3） 加入L2正则化。
