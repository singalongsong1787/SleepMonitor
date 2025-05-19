# sensor_data.py
import logging
import os
import csv
import shutil
from datetime import datetime
import pandas as pd
import numpy as np
import json
'''
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
file_path1 = "/data/data/com.morales.bnatest/files/chaquopy/data0.csv"


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
        file_path = "/data/data/com.morales.bnatest/files/chaquopy/data0.csv"

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

        logging.info(f"Saved intensities to {file_path}")

    except Exception as e:
        logging.error(f"Failed to save intensities: {e}")
'''




'''
###########################总目标：拿到各个区间###########################
###########################（1）整个区间，即sleep-wake###################
###########################（2）觉醒区间-会有多个########################
###########################（3）深睡区间-多个###########################
'''

wakeup_threshold=1
#对觉醒进行判断

file_path1 = "/data/data/com.morales.bnatest/files/chaquopy/data507.csv"

'''
function:拿到开始和结束的时间戳
input:1.开始时间
2.结束时间
output:1.时间戳标签
'''
def getTimestamp_startToEnd(start,end):

    timestamps_labels=[]

    start_hour = pd.Timestamp(start).hour
    start_end= pd.Timestamp(start).minute

    end_hour = pd.Timestamp(end).hour
    end_minute = pd.Timestamp(end).minute

    # 构建时间戳标签
    start_label = f"{start_hour:02d}:{start_end:02d}"
    end_label = f"{end_hour:02d}:{end_minute:02d}"

    # 添加到结果列表
    timestamps_labels.append((start_label, end_label))

    return timestamps_labels

'''
funtion:读取csv文件
input:无，直接读取file_path
output:1.accelerometer
       2.返回start-end区间
'''

def readCVS():
    df=pd.read_csv(file_path1)
    accelerometerX=df.iloc[:,1]
    accelerometerY=df.iloc[:,2]
    accelerometerZ=df.iloc[:,3]

    #获得时间戳标签
    timestamp=df.iloc[:,0]
    length_timestamp=len(timestamp)
    earliest_time=timestamp[0]
    latest_time=timestamp[length_timestamp-1]

    #对其求模长
    accelerometer=np.sqrt(accelerometerX**2 + accelerometerY**2 + accelerometerZ**2)
    #返回区间
    label_startToEnd=getTimestamp_startToEnd(earliest_time,latest_time)

    return accelerometer,label_startToEnd,earliest_time

'''
function:对觉醒区间进行判断
input: 1.数据data
       2.觉醒阈值
       3.时间窗间隔(5分钟)
output:1.觉醒区间
       2.均方根（忽略1的区间）
'''

#读取文件
data_accelerometer,labels_startToEnd,earliest_time=readCVS()

def find_intervals_above_threshold(data=data_accelerometer, threshold=1, interval=300):
    intervals = []
    # 修改初始化部分
    current_start = None
    rms_sum = 0
    valid_count = 0

    # 按每 interval 个元素一组进行遍历
    for i in range(0, len(data), interval):
        minute_data = data[i: i + interval]
        # 检查该组内是否有元素大于阈值
        has_above_threshold = any(value > threshold for value in minute_data)

        if has_above_threshold:
            if current_start is None:
                current_start = i
        else:
            if current_start is not None:
                # 记录当前区间
                intervals.append((round(current_start / 3600, 2), round(i / 3600, 2)))
                current_start = None

            # 计算该组的均方根贡献
            rms_sum += sum(x ** 2 for x in minute_data)
            valid_count += len(minute_data)

    # 处理列表末尾的情况
    if current_start is not None:
        intervals.append((round(current_start / 3600, 2), round(len(data) / 3600, 2)))

    if valid_count > 0:
        # 计算均方根
        rms = np.sqrt(rms_sum / valid_count)
    else:
        rms = 0

    return intervals, rms

wakeUp_intervals,rms=find_intervals_above_threshold()
logging.debug(f"wakeUp_intervals - {wakeUp_intervals}")

'''
funcion:计算平均RMS
input：  1.数据
output: 2.RMS数值
'''
def calculate_RMS(data):
    return np.sqrt(np.mean(np.square(data)))

'''
function:对数据的处理——判断深睡，如果为深睡赋值为1
input: 数据，默认为其原始数据
output:0-1的新数据
'''

def process_data(data=data_accelerometer):
    n = len(data)
    result = [0] * n
    for i in range(0, n, 60):
        window = data[i:i + 60]
        if len(window) == 60:
            RMS = calculate_RMS(window)
            # Bug 修复：填充 60 个 1
            if RMS >rms  or any(x > 1.5 * RMS for x in window):
                result[i:i + 60] = [1] * 60
        else:
            RMS = calculate_RMS(data[i:])
            # Bug 修复：填充合适数量的 1
            if RMS > rms or any(x <= 1.5 * RMS for x in data[i:]):
                result[i:] = [1] * len(data[i:])
    return result

deepSleep=process_data()#获取处理后的深睡数据
'''
function:获取其区间
input：数据，默认为深睡数据
output：区间，以小时计的
'''
def find_zero_intervals(data=deepSleep,min_length=600):
    intervals=[]
    start=None
    for i, value in enumerate(data):
        if value == 0:
            if start is None:
                start = i
        else:
            if start is not None:
                length = i - start
                if length >= min_length:
                    intervals.append((round(start / 3600, 2), round(i / 3600, 2)))
                start = None

                # 处理列表末尾是 0 的情况
    if start is not None:
        length = len(data) - start
        if length >= min_length:
            intervals.append((round(start / 3600, 2), round(len(data) / 3600, 2)))
    return intervals

deepSleep_intervals=find_zero_intervals()
logging.debug(f"deepSleep_intervals - {deepSleep_intervals}")

'''
function：返回时间戳
input:1.区间
output:1.时间戳标签（区间）
'''
def getTimestamp(intervals):
    # 将最早时刻转换为小时和分钟
    earliest_hour = pd.Timestamp(earliest_time).hour
    earliest_minute = pd.Timestamp(earliest_time).minute

    timestamps_labels=[]

    for start,end in intervals:
        # 计算开始和结束时间的总分钟数
        start_total_minutes = earliest_minute + int(start * 60)
        end_total_minutes = earliest_minute + int(end * 60)

        # 计算开始时间的小时和分钟，处理进位
        start_hour = (earliest_hour + start_total_minutes // 60) % 24
        start_minute = start_total_minutes % 60

        # 计算结束时间的小时和分钟，处理进位
        end_hour = (earliest_hour + end_total_minutes // 60) % 24
        end_minute = end_total_minutes % 60

        # 构建时间戳标签
        start_label = f"{start_hour:02d}:{start_minute:02d}"
        end_label = f"{end_hour:02d}:{end_minute:02d}"

        # 添加到结果列表
        timestamps_labels.append((start_label, end_label))
    return timestamps_labels

#加工区间

logger = logging.getLogger(__name__)
# 设置日志级别为 DEBUG
logger.setLevel(logging.DEBUG)

#最终返回结果——json文件
def getWakeUpAndDeepsleepInterval(start_end=labels_startToEnd,wakeup_interval=getTimestamp(wakeUp_intervals),deepsleep_interval=getTimestamp(deepSleep_intervals)):

    logger.debug(f"deepSleep_intervals - {deepSleep_intervals}")
    data={

        "start_end":start_end,
        "wakeup_interval":wakeup_interval,
        "deepsleep_interval":deepsleep_interval
    }

    json_data=json.dumps(data)
    logger.debug(f"json_data - {json_data}")
    #empty_csv_file(file_path1)
    #os.remove(file_path1)
    logging.info(f"remove successfully")

    return json_data

def remove_file():
    empty_csv_file(file_path1)
    os.remove(file_path1)

def empty_csv_file(file_path):
    try:
        # 以写入模式打开文件，这会清空文件内容
        with open(file_path, 'w', newline='') as file:
            # 写入表头（如果需要保留表头）
            # writer = csv.writer(file)
            # writer.writerow(['column1', 'column2', ...])
            pass
        print(f"文件 {file_path} 已清空。")
    except FileNotFoundError:
        print(f"错误：文件 {file_path} 未找到。")
    except Exception as e:
        print(f"发生未知错误：{e}")

def calculateLen():
    return len(data_accelerometer)





















