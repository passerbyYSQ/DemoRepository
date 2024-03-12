import os
import csv

input_file = 'csv/文档属性修复汇总.csv'  # 非标准的 CSV 文件路径
# 获取输入文件的文件名和扩展名
file_name, file_ext = os.path.splitext(os.path.basename(input_file))
output_file = os.path.join('res', file_name + ".csv")  # 标准的 CSV 文件路径

output_dir = os.path.dirname(output_file)
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

# 读取非标准的 CSV 文件
with open(input_file, 'r', newline='', encoding='utf-8') as file:
    reader = csv.reader(file, delimiter=',')
    rows = list(reader)

# 处理每个单元格中的换行符和前后空格
new_rows = []
for row in rows:
    if len(row) == 0:
        continue  # 跳过空行
    for i, cell in enumerate(row):
        cell = cell.replace('\n', '<NEWLINE>').strip()  # 去掉换行符和前后空格
        row[i] = cell
    d = len(rows[0]) - len(row)
    if d >= 0:
        row += [''] * d  # 补齐末尾缺失的列
    else:
        row = row[:d]
    new_rows.append(row)

# 另存为标准的 CSV 文件
with open(output_file, 'w', newline='', encoding='utf-8') as file:
    writer = csv.writer(file)
    writer.writerows(new_rows)
