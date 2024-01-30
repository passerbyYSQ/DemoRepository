import csv
import os
import re



def extract_java_file(file_path):
    file_dir, file_name = os.path.splitext(file_path)
    temp_file_path = file_path + ".tmp"
    # file_name = os.path.basename(file_path)
    result_list = []
    with open(file_path, 'r', encoding='utf-8') as java_file, open(temp_file_path, 'w', encoding='utf-8') as temp_file:
        in_comment = False
        for (row, line) in enumerate(java_file):
            idx = line.find('//')
            if idx != -1:
                line = line[:idx]

            lidx = line.find('/*')
            ridx = line.rfind('*/')

            if lidx != -1 and ridx != -1:
                extract_to_list(line[:lidx], result_list, file_name, row)
                extract_to_list(line[ridx + 2:], result_list, file_name, row)
                continue
            elif lidx != -1:
                in_comment = True  # 走入注释 前半部分
                extract_to_list(line[:lidx], result_list, file_name, row)
                continue
            elif lidx != -1:
                in_comment = False  # 走出注释。 后半部分
                extract_to_list(line[ridx + 2:], result_list, file_name, row)
                continue
            if in_comment:
                continue  # 注释行，直接跳过
            extract_to_list(line, result_list, file_name, row)

    # res_list.sort(key=lambda row: row[1])
    return result_list  # 文件名, 行号, 中文信息


def extract_to_list(str, result_list, file_name, row):
    matched_list = re.findall(r'"([^"\\]*[\u4E00-\u9FA5]+[^"\\]*)"', str)
    for matched_text in matched_list:
        result_list.append([file_name, row + 1, matched_text])


def extract_dir_to_csv(dir_path):
    result_list = []
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            _, ext = os.path.splitext(file)
            if ext != '.java':
                continue
            result_list += extract_java_file(os.path.join(root, file))
    # 另存为标准的 CSV 文件
    output_file = os.path.join('res', "Java代码中的中文报错信息汇总.csv")  # 标准的 CSV 文件路径
    with open(output_file, 'w', newline='', encoding='utf-8') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerows(result_list)


if __name__ == '__main__':
    # extract_dir_to_csv('E:\Project\ZW\sucore\core\server')
    extract_java_file('E:\Project\ZW\sucore\core\modules\omf\src\main\java\\tech\sucore\ipc\mi\OMFInvoker.java')

    print("*12".startswith('//'))
