import logging
import os
import csv
import shutil
from datetime import datetime
import math

sampling = 50
file_path =  "/data/data/com.morales.bnatest/files/chaquopy/gyroscope_data_test514.csv"

#检查文件是否存在
directory = os.path.dirname(file_path)
if not os.path.exists(directory):
    os.makedirs(directory)
    logging.info(f"creat successfully")
else:
    logging.info(f"creat ok")

def saveDataToCsv(x,y,z):
    '''
    添加数据，直接保存模长就行了吧
    :param x: x轴数据
    :param y: y轴数据
    :param z: z轴数据
    :return: 添加数据到Csv
    '''

    #求模长
    global gyroscope_data
    mfd= math.sqrt(x ** 2 + y ** 2 + z ** 2)
    timestamp = datetime.now().strftime("%H:%M:%S")
    file_exists = os.path.exists(file_path)

    with open(file_path,mode='a',newline='') as file:
        writer = csv.writer(file)
        if not file_exists:
            writer.writerow(['Timestamp','gyroscope_data'])
            #logging.info(f"成功写入")
        writer.writerow([timestamp,mfd])
        #logging.info(f"成功写入")




