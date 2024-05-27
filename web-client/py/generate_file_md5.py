import hashlib
import sys
import time

import csv
import os


def calculate_md5(file_path):
    # 创建一个 MD5 对象
    md5_hash = hashlib.md5()

    # 以二进制方式读取文件内容，逐块更新 MD5 值
    with open(file_path, 'rb') as file:
        for chunk in iter(lambda: file.read(4096), b''):
            md5_hash.update(chunk)

    # 返回计算得到的 MD5 值
    return md5_hash.hexdigest()


def generate_file_md5(dir_path):
    start_time = time.time()

    rows = [['文件名', 'MD5', '路径']]
    for root, dirs, files in os.walk(dir_path):
        for file_name in files:
            file_path = os.path.join(root, file_name)
            file_md5 = calculate_md5(file_path)
            rows.append([file_name, file_md5, file_path])
            print(file_path)

    with open('file_md5.csv', 'w', newline='', encoding='utf-8') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerows(rows)

    end_time = time.time()
    print("耗时：", (end_time - start_time), "秒")


if __name__ == '__main__':
    print(sys.argv)
    if len(sys.argv) < 2:
        raise Exception('必须指定一个目录的路径')
    dir_path = sys.argv[1]
    if not os.path.isdir(dir_path):
        raise Exception('必须指定一个目录的路径')
    generate_file_md5(dir_path)
