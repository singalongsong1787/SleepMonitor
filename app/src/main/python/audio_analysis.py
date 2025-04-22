import numpy as np
def analyze_audio(byte_array):
    try:
        # 将字节数组转换为 numpy 的 short 数组
        audio_data = bytearray(byte_array)
        print("接收到的音频数据（字节形式展示部分内容）:", audio_data[:100])

        return "数据输出成功"
    except Exception as e:
        print(f"处理音频数据时出错: {e}")
        return "数据输出失败"