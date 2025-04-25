import logging
import os
import csv
import shutil
from datetime import datetime
import math
from scipy.ndimage import gaussian_filter1d
from scipy.signal import find_peaks
import numpy as np

sampling = 50


#存储强度
intensity=[]
timestamp_list = []



def gaussian_filter(data, sigma=10):
    data = np.array(data, dtype=np.float64)
    # 使用scipy的gaussian_filter1d进行滤波
    filtered_data = gaussian_filter1d(data, sigma=sigma, mode='mirror')
    return filtered_data


def detect_peaks_with_neighbors(gyroscope_filtered, min_height=0.005, min_distance=300, neighbor_range=100, min_neighbors=10):
    # 确保输入是一维numpy数组
    gyroscope_filtered = np.array(gyroscope_filtered)
    # 初始峰值检测
    peaks, _ = find_peaks(gyroscope_filtered, height=min_height, distance=min_distance)

    # 过滤峰值：检查周围点是否满足条件
    filtered_peaks = []
    half_range = neighbor_range // 2  # 前后各检查50个点

    for peak in peaks:
        # 定义检查范围，注意边界处理
        start = max(0, peak - half_range)
        end = min(len(gyroscope_filtered), peak + half_range + 1)

        # 计算范围内大于min_height的点数
        neighbor_data = gyroscope_filtered[start:end]
        num_above_threshold = np.sum(neighbor_data > min_height)
        # 如果满足条件（周围有至少3个点大于min_height），保留该峰值
        if num_above_threshold >= min_neighbors:
            filtered_peaks.append(peak)

    return np.array(filtered_peaks)

def dectionRoll(x,y,z):
    #求模长
    global intensity
    global timestamp_list
    mfd= math.sqrt(x ** 2 + y ** 2 + z ** 2)
    intensity.append(mfd)

    timestamp = datetime.now().strftime("%H:%M:%S")
    timestamp_list.append(timestamp)

    #一个窗口的长度
    window_length = 20 * sampling #1分钟
    logging.info(f"{len(intensity)}")

    #对窗口数据进行处理
    if len(intensity)==window_length:
        logging.info(f"开启翻身判断")
        window = np.array(intensity)#list--->numpy
        nptimestamp_list = np.array(timestamp_list)

        #清空列表
        # 清除旧窗口数据
        intensity.clear()
        timestamp_list.clear()
        #logging.info(f"clear successfully")


        window_filtered = gaussian_filter(window)#高斯滤波
        #logging.info(f"Gaussion filter successfully")
        #对滤波完成的数组找寻局部峰值
        filtered_peaks = detect_peaks_with_neighbors(window_filtered, min_height=0.005, min_distance=500,
                                                     neighbor_range=100, min_neighbors=50)
        logging.info(f"翻身result:{filtered_peaks}")

        if len(filtered_peaks) == 0 :
            logging.info(f"翻身结果为None")
            return None

        #返回时间
        time_roll = np.array(nptimestamp_list[filtered_peaks])
        #返回强度
        intensity_roll = np.array(window_filtered[filtered_peaks])

        result = list(zip(time_roll,intensity_roll))
        logging.info(f"翻身结果为{result}")

        return  result


    return None
