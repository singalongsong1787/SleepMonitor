import logging
import math
import os
import csv
import shutil
from datetime import datetime


# 假设采样率为 50，你可以根据实际情况修改
sampling = 50

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger('GyroscopeLogger')


# 初始化三个列表用于存储陀螺仪三个轴的数据
gyroscope_data = []
indensity = []

#用于存储差分变换后的值

def addGyroscope_data(x,y,z):

    '''
    添加陀螺仪数据到相应的列表中
    '''
    file_path = "/data/data/com.morales.bnatest/files/chaquopy/gyroscope417.csv"
    timestamp = datetime.now().strftime("%H:%M:%S")

    # 确保目录存在
    directory = os.path.dirname(file_path)
    if not os.path.exists(directory):
        os.makedirs(directory)
        logging.info(f"creat successfully")
    else:
        logging.info(f"creat ok")

    global gyroscope_data
    mfd= math.sqrt(x ** 2 + y ** 2 + z ** 2)
    gyroscope_data.append(mfd)
    logging.info(f"mfd is{mfd}")

    if len(gyroscope_data) == sampling:
        diff_mfd = [gyroscope_data[i+1] - gyroscope_data[i] for i in range(len(gyroscope_data)-1)]
        logging.info(f"diff_mfd is{diff_mfd}")

        indensity = max(diff_mfd)#indensity.append(diff_mfd)
        logging.info(f"indensity is{indensity}")
        gyroscope_data = []
        # 检查文件是否存在，如果不存在则创建并写入表头

        file_exists = os.path.exists(file_path)
        logging.info(f"file_exists{file_exists}")
        with open(file_path,mode='a',newline='') as file:
            writer = csv.writer(file)
            if not file_exists:
                writer.writerow(['Timestamp','gyroscope_data'])
                logging.info(f"成功写入")
            writer.writerow([timestamp,indensity])
            logging.info(f"成功写入")












