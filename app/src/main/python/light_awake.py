import logging
import os
import csv
import shutil
from datetime import datetime
import math

# 用于存储加速度计数据的三个列表
accelerometer_x_data = []
accelerometer_y_data = []
accelerometer_z_data = []

#用于存储差分变换后最大值的三个列表
intensityX=[]
intensityY=[]
intensityZ=[]

intensityList=[]

#采样率
sampling=50

def diff_data(x,y,z):
    global accelerometer_x_data, accelerometer_y_data, accelerometer_z_data
    accelerometer_x_data.append(x)
    logging.info(f"accelerometerX的数据为{intensityX}")
    accelerometer_y_data.append(y)
    accelerometer_z_data.append(z)

    if len(accelerometer_x_data) == sampling and len(accelerometer_y_data) == sampling and len(accelerometer_z_data) == sampling:
        # 对 X 轴数据进行差分变换并找出最大值
        diff_x = [accelerometer_x_data[i + 1] - accelerometer_x_data[i] for i in range(len(accelerometer_x_data) - 1)]
        max_x = max(diff_x)
        logging.info(f"Calculated max_x: {max_x:.4f}")  # 格式化为 4 位小数
        intensityX.append(max_x)
        logging.info(f"intenstiyX的数据为{intensityX}")

        # 对 Y 轴数据进行差分变换并找出最大值
        diff_y = [accelerometer_y_data[i + 1] - accelerometer_y_data[i] for i in range(len(accelerometer_y_data) - 1)]
        max_y = max(diff_y)
        intensityY.append(max_y)

        # 对 Z 轴数据进行差分变换并找出最大值
        diff_z = [accelerometer_z_data[i + 1] - accelerometer_z_data[i] for i in range(len(accelerometer_z_data) - 1)]
        max_z = max(diff_z)
        intensityZ.append(max_z)

        # 清空三个加速度计数据列表，准备接收新数据
        accelerometer_x_data = []
        accelerometer_y_data = []
        accelerometer_z_data = []

def judge_data():
    global intensityList
    try:
        intensity = math.sqrt(intensityX[-1]**2 + intensityY[-1]**2 + intensityZ[-1]**2)
        intensityList.append(intensity)
        logging.info(f"intensityList的数据为{intensityList}")
        if len(intensityList) == 30:
            RMS = calculate_rms(intensityList)
            if any(x > (0.1) for x in intensityList):
                intensityList = []
                return True
            else:
                intensityList = []
                return False
        else:
            return False
    except Exception as e:
        logging.error(f"judge_data 函数出错: {e}")
        return False


def calculate_rms(numbers):
    if not numbers:  # 处理空列表
        return 0
    # 计算平方的平均值
    mean_square = sum(x * x for x in numbers) / len(numbers)
    # 返回平方根
    return math.sqrt(mean_square)