import tensorflow as tf

# 自定义 STFT 层
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

# 加载 Keras 模型，将自定义层作为自定义对象传递
model_path = "/workspace/SonrinModel2.h5"
custom_objects = {'STFTLayer': STFTLayer}
model = tf.keras.models.load_model(model_path, custom_objects=custom_objects)

# 转换为 TensorFlow Lite 模型
converter = tf.lite.TFLiteConverter.from_keras_model(model)

tflite_model = converter.convert()

# 保存转换后的模型
tflite_model_path = "/workspace/SonrinModel.tflite"
with open(tflite_model_path, 'wb') as f:
    f.write(tflite_model)

print("OK")