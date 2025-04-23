import logging
import os
import csv
import shutil
from datetime import datetime
import math


sampling = 50
file_path = "/data/data/com.morales.bnatest/files/chaquopy/sensor_data.csv"

directory = os.path.dirname(file_path)
if not os.path.exists(directory):
    os.makedirs(directory)



def saveSensorDataToCSV(ax,ay,az,gx,gy,gz):

    timestamp = datetime.now().strftime("%H:%M:%S")
    file_exists = os.path.exists(file_path)

    with open(file_path,mode='a',newline='') as file:
        writer = csv.writer(file)
        if not file_exists:
            writer.writerow(['Timestamp','accelerometerX','accelerometerY','accelerometerZ',
                             'GyroscopeX','GyroscopeY','GyroscopeZ'])
            logging.info(f"成功写入")
        writer.writerow([timestamp,ax,ay,az,gx,gy,gz])
        logging.info(f"成功写入传感器")
