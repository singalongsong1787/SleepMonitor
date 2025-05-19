# 鼾声模型性能评估

## 在训练集（Snoring Dataset）中剪枝前后的<span style="color:red">损失值、准确率、召回率和精确率</span>的表现

<div align="center">
  <table>
    <tr>
      <td align="center">
<img src=".\images\model_snoreRecognition\Preformance_loss.png" 
width="100%"></td>
      <td align="center">
<img src=".\images\model_snoreRecognition\Preformance_accuracy.png" 
width="100%"></td>
    </tr>
    <tr>
      <td align="center">
<img src=".\images\model_snoreRecognition\Preformance_recall.png" 
width="100%"></td>
      <td align="center">
<img src=".\images\model_snoreRecognition\Preformance_precision.png" 
width="100%"></td>
    </tr>
  </table>
</div>

</div>

## TFlite模型在验证集上的表现

### <span style="color: red">（1） 混淆矩阵</span>

<center>
 <img src=".\images\model_snoreRecognition\Confusion.png" width="400" style="margin: 0 10px;">
</center>

 <span style="color:blue">**对FP进行查看，即哪些种类声音被误检测为鼾声**</span>

<center>
 <img src=".\images\model_snoreRecognition\FP__test.png" width="500" style="margin: 0 10px;">
 <img src=".\images\model_snoreRecognition\FP_test_2.png" width="500" style="margin: 0 10px;">
</center>

### <span style="color:red">（2） P_R和ROC曲线</span>

<center>
 <img src=".\images\model_snoreRecognition\P_R.png" width="300" style="margin: 0 10px;">
 <img src=".\images\model_snoreRecognition\ROC.png" width="300" style="margin: 0 10px;">
</center>

## 模型的所占内存空间大小对比

<center>
 <img src=".\images\model_snoreRecognition\ModelSize.png" width="300" style="margin: 0 10px;">
</center>
