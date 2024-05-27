import os
import csv

input_files = [
    # 'csv/bo_CAD Drawing0.xls',
    # 'csv/bo_CAD Drawing1.xls',
    # 'csv/bo_CAD Drawing2.xls',
    # 'csv/bo_CAD Drawing3.xls',
    # 'csv/bo_CAD Drawing4.xls',
    # 'csv/bo_CAD Drawing5.xls',
    # 'csv/bo_CAD Drawing6.xls',
    # 'csv/bo_CAD Drawing7.xls',
    # 'csv/bo_CAD Drawing8.xls',
    # 'csv/bo_CAD Drawing9.xls',
    # 'csv/bo_CAD Drawing10.xls',
    # 'csv/bo_CAD Drawing11.xls',
    # 'csv/bo_CAD Drawing12.xls',
    # 'csv/bo_CAD Drawing13.xls',
    # 'csv/bo_CAD Drawing0.xls',
    # 'csv/rel-b2b_EBOM.xls',
    # 'csv/bo_Document_ALL_ALL.xls',
    # 'csv/bo_ZTENEWTZD_ALL_ALL.xls',
    # 'csv/bo_Document0.xls',
    # 'csv/bo_Document1.xls',
    # 'csv/bo_Document2.xls',
    # 'csv/bo_Document3.xls'
    # ('csv/rel-b2b_Reference Document.xls', 'gbk', '\t')
    # ('csv/rel-b2b_Classified Item.xls', 'gbk', '\t')
    ('csv/文档属性修复汇总.csv', 'utf-8', ',')
               ]  # 非标准的 CSV 文件路径

if __name__ == '__main__':
    for input_file, enc, delimiter in input_files:
        # 获取输入文件的文件名和扩展名
        file_name, file_ext = os.path.splitext(os.path.basename(input_file))
        output_file = os.path.join('res', file_name + ".csv")  # 标准的 CSV 文件路径

        output_dir = os.path.dirname(output_file)
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)

        # 读取非标准的 CSV 文件
        with open(input_file, 'r', newline='', encoding=enc) as file:
            reader = csv.reader(file, delimiter=delimiter)
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
