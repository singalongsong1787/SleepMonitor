#使用ESC50数据集进行模型性能的测试

#对ESC50数据集文件进行预处理，将esc50文件裁剪到1s
import os
import pandas as pd
import librosa
import numpy as np
import soundfile as sf
import tensorflow as tf
import tensorflow_io as tfio
from sklearn.metrics import roc_curve, auc
import matplotlib.pyplot as plt

#预测阈值
threshold = 0.5
#混淆矩阵数值
confusionMatrix=[0]*4

#导入csv文件
dataset_path = '/workspace/毕业设计BMC/esc50/ESC-50-master'  # 请替换为实际的 ESC-50 数据集路径
metadata_path = os.path.join(dataset_path, 'meta', 'esc50.csv')
audio_path = os.path.join(dataset_path, 'audio_onesec')

#加载模型
tflite_model_path = "/workspace/SonrinModel.tflite"
interpreter = tf.lite.Interpreter(model_path=tflite_model_path)
interpreter.allocate_tensors()

# 获取输入和输出张量的索引
input_details = interpreter.get_input_details()
print(input_details)
output_details = interpreter.get_output_details()
# 打印模型期望的输入维度
print("模型期望的输入维度:", input_details[0]['shape'])

#读取元数据文件
df = pd.read_csv(metadata_path)

# 加载 ESC-50 数据集并进行验证
correct_predictions = 0
total_predictions = 0

# 选取第一个音频文件进行测试（这里只是示例，你可以根据需要调整）
index = 0
row = df.iloc[index]
file_name = row['filename']
true_label = row['category']  # 假设 category 列为标签列，可根据实际情况调整

# 用于存储真实标签和预测概率
y_true = []
y_scores = []


def load_wav_16k_mono(filename):
    # 检查路径是否为文件
    if not os.path.isfile(filename):
        raise ValueError(f"{filename} 不是一个有效的文件路径，请检查。")
    try:
        file_contents = tf.io.read_file(filename)
        wav, sample_rate = tf.audio.decode_wav(file_contents, desired_channels=1)
        wav = tf.squeeze(wav, axis=-1)
        sample_rate = tf.cast(sample_rate, dtype=tf.int64)
        wav = tfio.audio.resample(wav, rate_in=sample_rate, rate_out=16000)
        return wav
    except Exception as e:
        print(f"加载音频文件 {filename} 时出错: {e}")
        return None


def pad_audio(sample):
    sample_length = tf.shape(sample)[0]
    zero_padding = tf.zeros([16000 - sample_length], dtype=tf.float32)
    wav = tf.concat([zero_padding, sample], 0)
    return wav

for index,row in df.iterrows():
    file_name=row['filename']
    true_label=row['category']
    print(true_label)
    #构建音频路径
    audio_file_path = os.path.join(audio_path, file_name)
    
    #读取音频文件
    wav=load_wav_16k_mono(audio_file_path)
    wav=pad_audio(wav)

    #增加一个维度匹配输出
    wav=tf.expand_dims(wav,axis=0)

    # 确保输入数据类型与模型期望的类型一致
    input_dtype = input_details[0]['dtype']
    wav = tf.cast(wav, dtype=input_dtype)
    # 设置输入张量
    interpreter.set_tensor(input_details[0]['index'], wav.numpy())

    # 运行推理
    interpreter.invoke()

    #获取预测概率
    prediction = interpreter.get_tensor(output_details[0]['index'])

    #存储真实标签和预测概率
    y_true.append(1 if true_label=='snoring' else 0)
    y_scores.append(prediction[0][0])



    #直接使用唯一的概率值进行判断
    #(1)真实情况为真
    if true_label=='snoring':
        if prediction[0][0]>threshold:
            confusionMatrix[0]+=1
        else:
            confusionMatrix[1]+=1
    else:#(2)实际为假
        if prediction[0][0]>threshold:
            confusionMatrix[2]+=1
        else:
            confusionMatrix[3]+=1

print(confusionMatrix)

# 计算 ROC 曲线
fpr, tpr, thresholds = roc_curve(y_true, y_scores)
# 计算 AUC 值
roc_auc = auc(fpr, tpr)


# 转换为numpy数组
np_y_true = np.array(y_true)
np_y_scores = np.array(y_scores)

# 保存为.npy文件
np.save('y_true.npy', np_y_true)
np.save('y_scores.npy', np_y_scores)



# 绘制 ROC 曲线
plt.figure()
plt.plot(fpr, tpr, color='darkorange', lw=2, label='ROC curve (area = %0.2f)' % roc_auc)
plt.plot([0, 1], [0, 1], color='navy', lw=2, linestyle='--')
plt.xlim([0.0, 1.0])
plt.ylim([0.0, 1.05])
plt.xlabel('False Positive Rate')
plt.ylabel('True Positive Rate')
plt.title('Receiver operating characteristic example')
plt.legend(loc="lower right")
plt.savefig('roc_curve.png')
plt.show()

print("混淆矩阵:", confusionMatrix)
print("AUC 值:", roc_auc)



    
   



'''
# 增加一个维度以匹配模型输入
padded_segment = tf.expand_dims(padded_segment, axis=0)

# 确保输入数据类型与模型期望的类型一致
input_dtype = input_details[0]['dtype']
padded_segment = tf.cast(padded_segment, dtype=input_dtype)
# 设置输入张量
interpreter.set_tensor(input_details[0]['index'], padded_segment.numpy())

# 运行推理
interpreter.invoke()

# 获取输出张量
prediction = interpreter.get_tensor(output_details[0]['index'])
print(prediction)'''









