import os
import tensorflow as tf
import tensorflow_io as tfio
import math
import numpy as np
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D, Dense, Flatten, MaxPooling2D, InputLayer
from tensorflow.keras.callbacks import EarlyStopping

# 数据加载和预处理部分保持不变
SNORING_DATA_PATH = os.path.join('/workspace/毕业设计BMC/Snoring Dataset1', '1')
NOT_SNORING_DATA_PATH = os.path.join('/workspace/毕业设计BMC/Snoring Dataset1', '0')


def load_wav_16k_mono(filename):
    file_contents = tf.io.read_file(filename)
    wav, sample_rate = tf.audio.decode_wav(file_contents, desired_channels=1)
    wav = tf.squeeze(wav, axis=-1)
    sample_rate = tf.cast(sample_rate, dtype=tf.int64)
    wav = tfio.audio.resample(wav, rate_in=sample_rate, rate_out=16000)
    return wav


def preprocess(file_path, label):
    wav = load_wav_16k_mono(file_path)
    wav = wav[:16000]
    zero_padding = tf.zeros([16000] - tf.shape(wav), dtype=tf.float32)
    wav = tf.concat([zero_padding, wav], 0)
    return wav, label


pos = tf.data.Dataset.list_files(SNORING_DATA_PATH + '/*.wav')
neg = tf.data.Dataset.list_files(NOT_SNORING_DATA_PATH + '/*.wav')

positives = tf.data.Dataset.zip((pos, tf.data.Dataset.from_tensor_slices(tf.ones(len(pos)))))
negatives = tf.data.Dataset.zip((neg, tf.data.Dataset.from_tensor_slices(tf.zeros(len(neg)))))
data = positives.concatenate(negatives)

data = data.map(preprocess)
data = data.cache()
data = data.shuffle(buffer_size=1000)
data = data.batch(64)
data = data.prefetch(8)

train = data.take(math.ceil(len(data) * .7))
test = data.skip(math.ceil(len(data) * .7)).take(math.floor(len(data) * .3))

samples, labels = train.as_numpy_iterator().next()
input_shape = (16000,)


# 自定义STFT层
class STFTLayer(tf.keras.layers.Layer):
    def __init__(self, frame_length=320, frame_step=32, **kwargs):
        super(STFTLayer, self).__init__(**kwargs)
        self.frame_length = frame_length
        self.frame_step = frame_step

    def call(self, inputs):
        stft = tf.signal.stft(inputs, frame_length=self.frame_length, frame_step=self.frame_step)
        spectrogram = tf.abs(stft)
        spectrogram = tf.expand_dims(spectrogram, axis=-1)
        return spectrogram

    def get_config(self):
        config = super(STFTLayer, self).get_config()
        config.update({
            'frame_length': self.frame_length,
            'frame_step': self.frame_step
        })
        return config


import tensorflow_model_optimization as tfmot
# 设置剪枝策略
pruning_schedule = tfmot.sparsity.keras.PolynomialDecay(
    initial_sparsity=0.0,  # 初始稀疏度
    final_sparsity=0.5,  # 最终稀疏度
    begin_step=0,  # 开始剪枝的步骤
    end_step=1000,  # 结束剪枝的步骤
    frequency=100  # 每多少步更新一次剪枝
)


from tensorflow.keras.regularizers import l2

# 构建包含STFT层的模型
model = Sequential()
model.add(InputLayer(input_shape=input_shape))
model.add(STFTLayer(frame_length=320, frame_step=32))
model.add(tfmot.sparsity.keras.prune_low_magnitude(Conv2D(16, (3, 3), activation='relu', input_shape=input_shape, kernel_regularizer=l2(0.001)), pruning_schedule=pruning_schedule))
# 第一个池化层
model.add(MaxPooling2D(pool_size=(2, 2)))
# 第二个卷积层
model.add(tfmot.sparsity.keras.prune_low_magnitude(Conv2D(16, (3, 3), activation='relu', kernel_regularizer=l2(0.001)), pruning_schedule=pruning_schedule))
# 第二个池化层
model.add(MaxPooling2D(pool_size=(2, 2)))
# 展平层
model.add(Flatten())
# 第一个全连接层
model.add(Dense(128, activation='relu', kernel_regularizer=l2(0.001)))
# 输出层
model.add(Dense(1, activation='sigmoid'))

# 编译模型
model.compile(optimizer='adam', loss='binary_crossentropy', metrics=[tf.keras.metrics.Recall(),
                                                                      tf.keras.metrics.Precision(),
                                                                     ])

# 训练模型
# 训练模型，添加 UpdatePruningStep 回调函数
callbacks = [tfmot.sparsity.keras.UpdatePruningStep()]
hist = model.fit(train, epochs=10, validation_data=test, callbacks=callbacks)

# 移除剪枝包装器
model = tfmot.sparsity.keras.strip_pruning(model)
model.summary()


'''
# 提取每次 epoch 的指标并存入列表
train_losses = hist.history['loss']
train_recalls = hist.history['recall']
train_precisions = hist.history['precision']
#train_accuracies = hist.history['accuracy']
val_losses = hist.history['val_loss']
val_recalls = hist.history['val_recall']
val_precisions = hist.history['val_precision']
#val_accuracies = hist.history['val_accuracy']


# 打印列表查看结果
print("训练集损失:", train_losses)
print("训练集召回率:", train_recalls)
print("训练集精确率:", train_precisions)
#print("训练集准确率", train_accuracies)
print("验证集损失:", val_losses)
print("验证集召回率:", val_recalls)
print("验证集精确率:", val_precisions)
#print("验证集准确率", val_accuracies)

# 自定义量化配置，排除自定义层
'''



# 保存模型
model_name = 'SonrinModel2.h5'
save_path = './workspace/' + model_name
tf.keras.models.save_model(model, save_path)

if os.path.exists(save_path):
    print(f"模型已成功保存到: {save_path}")
else:
    print(f"模型保存失败，路径 {save_path} 不存在对应的文件。")








