# 算法概述

## <span style="color:red">**鼾声识别**</span>

基于**卷积神经网络**（CNN）构建鼾声识别的算法模型，使用tensorflow学习框架，语言Python。

### （1）数据集

收集两个公开数据集进行对模型的训练和性能验证。

+ [Snoring Dataset](https://www.kaggle.com/datasets/tareqkhanemu/snoring/data):作为一个训练集，该数据集包含鼾声和非鼾声样本各500个，是一个二分类数据集。

+ [ESC-50](https://github.com/karolpiczak/ESC-50)：作为一个验证集，该数据集含有5大类，50小类，是一个50分类数据集。

### （2）特征处理与CNN模型的创建

对每段音频进行短时傅里叶变换，依此作为模型的输入部分。构建两层卷积。

参考：[Deep-learning Snoring Detector - version 2.0](https://www.kaggle.com/code/orannahum/deep-learning-snoring-detector-version-2-0)(author：oran nahum)

主要做出改进：（1） 将短时傅里叶变换融入到CNN模型中，使之作为单独一层，即模型的输入为一维的音频数据，不在为二维的频谱图。（短时傅里叶变换在模型内部完成）

                            （2） 模型加入<span style="color:blue">**池化层**</span>，这将极大的减少模型参数数量，在<span style="color:blue">移动端部署中十分重要。</span>

                            （3） 加入L2正则化。

<span style="color:red">**训练代码：**</span>[modelTrain.py](modelTrain.py)

### （3） [模型压缩](https://zhuanlan.zhihu.com/p/608915925)

<span  style="color:blue">模型压缩：在不影响模型性能的基础上，尽可能的减少模型的数据量，过大的模型数据量将会导致内存泄漏等一系列问题。</span>

具体的方法：[模型剪枝](https://zhuanlan.zhihu.com/p/622519997)

<span style="color:red">**训练代码：**</span>[modelPruning.py](modelPruning.py)

### （4）转换为tflite模型

[TensorFlow Lite模型](https://www.tensorflow.org/lite/guide?hl=zh-cn#get_started):可以帮助开发者在移动设备、嵌入式设备和IoT设备上运行算法模型，以实现移动端部署。

（<span style="color:red">注：该模型其提供了诸多压缩方案，转换的本质也是在压缩，不一定非要对模型进行压缩，该模型官方提供了几个示例程序。</span>）

<span style="color:red">**转换代码：**</span>[convertTF.py](convertTF.py) 

### 性能评估

[查看详情](model_perfermanceEvaluation.md)

<span style="color:red">**转换代码：**</span>[modelTest.py](modelTrain.py)

## <span style="color:blue">**不足及可能解决方案**</span>

+ 模型的泛化能力可以进一步的提升。可以通过提高原模型的通道数量、层数，在压缩中使用**剪枝和量化**。
+ ESC50数据集是一个非平衡的数据集，鼾声仅占比2%，准确率和P-R曲线表现差。可以进一步补充鼾声数据

[audioSet](https://research.google.com/audioset/dataset/snoring.html)有专门的鼾声类，但提取较为困难，采样率模糊

[男女鼾声数据集](https://www.kaggle.com/datasets/orannahum/female-and-male-snoring/data)

## 睡眠分期算法
