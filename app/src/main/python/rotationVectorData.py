import logging
import os
import csv
import shutil
from datetime import datetime
import math


sampling = 50
file_path = "/data/data/com.morales.bnatest/files/chaquopy/rotation_data_test424.csv"

directory = os.path.dirname(file_path)
if not os.path.exists(directory):
    os.makedirs(directory)



def saveRotationVectorDataToCSV(azimuth,pitch,roll):

    timestamp = datetime.now().strftime("%H:%M:%S")
    file_exists = os.path.exists(file_path)

    with open(file_path,mode='a',newline='') as file:
        writer = csv.writer(file)
        if not file_exists:
            writer.writerow(['Timestamp','azimuth','pitch','roll'])
            logging.info(f"成功写入")
        writer.writerow([timestamp,azimuth,pitch,roll])
        logging.info(f"成功写入传感器")