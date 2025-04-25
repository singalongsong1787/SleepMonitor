import logging
import os
import csv
import shutil
from datetime import datetime
# 用于存储加速度计数据的三个列表
accelerometer_x_data = []
accelerometer_y_data = []
accelerometer_z_data = []

#用于存储差分变换后最大值的三个列表
intensityX=[]
intensityY=[]
intensityZ=[]

#采样率
sampling=50

#写入文件
file_path1 = "/data/data/com.morales.bnatest/files/chaquopy/data1.csv"


# 配置日志
logging.basicConfig(level=logging.INFO)

# 用于存储加速度计数据的三个列表
accelerometer_x_data = []
accelerometer_y_data = []
accelerometer_z_data = []




# 用于存储差分变换后最大值的三个列表
intensityX = []
intensityY = []
intensityZ = []

# 假设采样率为 50，你可以根据实际情况修改
sampling = 50


def add_and_save_accelerometer_data(x, y, z):
    """
    添加加速度计数据到对应的列表中，当数据量达到采样率时进行差分变换、保存数据并清空列表
    """
    global accelerometer_x_data, accelerometer_y_data, accelerometer_z_data
    accelerometer_x_data.append(x)
    accelerometer_y_data.append(y)
    accelerometer_z_data.append(z)

    # 输出日志
    logging.debug(f"Received accelerometer data - X: {x}, Y: {y}, Z: {z}")

    # 检查三个列表的长度是否达到采样率
    if len(accelerometer_x_data) == sampling and len(accelerometer_y_data) == sampling and len(accelerometer_z_data) == sampling:
        # 对 X 轴数据进行差分变换并找出最大值
        diff_x = [accelerometer_x_data[i + 1] - accelerometer_x_data[i] for i in range(len(accelerometer_x_data) - 1)]
        max_x = max(diff_x)
        logging.info(f"Calculated max_x: {max_x:.4f}")  # 格式化为 4 位小数
        intensityX.append(max_x)

        # 对 Y 轴数据进行差分变换并找出最大值
        diff_y = [accelerometer_y_data[i + 1] - accelerometer_y_data[i] for i in range(len(accelerometer_y_data) - 1)]
        max_y = max(diff_y)
        intensityY.append(max_y)

        # 对 Z 轴数据进行差分变换并找出最大值
        diff_z = [accelerometer_z_data[i + 1] - accelerometer_z_data[i] for i in range(len(accelerometer_z_data) - 1)]
        max_z = max(diff_z)
        intensityZ.append(max_z)

        save_intensities()

        # 清空三个加速度计数据列表，准备接收新数据
        accelerometer_x_data = []
        accelerometer_y_data = []
        accelerometer_z_data = []


def save_intensities():
    try:
        # 指定文件路径
        file_path = "/data/data/com.morales.bnatest/files/chaquopy/data1.csv"

        timestamp = datetime.now().strftime("%H:%M:%S")

        # 确保目录存在
        directory = os.path.dirname(file_path)
        if not os.path.exists(directory):
            os.makedirs(directory)


        # 检查文件是否存在，如果不存在则创建并写入表头
        file_exists = os.path.exists(file_path)
        with open(file_path, mode='a', newline='') as file:
            writer = csv.writer(file)
            if not file_exists:
                writer.writerow(['Timestamp','IntensityX', 'IntensityY', 'IntensityZ'])
            # 写入最新的 intensityX、intensityY 和 intensityZ 值
            writer.writerow([timestamp,intensityX[-1], intensityY[-1], intensityZ[-1]])

       # logging.info(f"Saved intensities to {file_path}")

    except Exception as e:
        logging.error(f"Failed to save intensities: {e}")


def get_accelerometer_data():
    """
    获取加速度计数据
    """
    return accelerometer_x_data, accelerometer_y_data, accelerometer_z_data



def get_intensity_lists():
    """
    获取差分变换后最大值的三个列表
    """
    return intensityX, intensityY, intensityZ

