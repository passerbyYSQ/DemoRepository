import csv
import os


def join_csv(src_dir, out_path):
    # 获取输入目录中的所有CSV文件
    csv_names = [file for file in os.listdir(src_dir) if file.endswith('.csv')]
    with open(out_path, 'w', newline='', encoding='utf-8') as out_csv:
        writer = csv.writer(out_csv)
        set1 = set()
        for csv_name in csv_names:
            csv_path = os.path.join(src_dir, csv_name)
            with open(csv_path, 'r', newline='', encoding='utf-8') as csv_file:
                reader = csv.reader(csv_file, delimiter=',')
                rows = list(reader)
                for row in rows[1:]:
                    key = row[0] + '_' + row[1]
                    if key in set1:
                        continue
                    writer.writerow(row)
                    set1.add(key)


def filter_csv(csv_path, out_path):
    with open(out_path, 'w', newline='', encoding='utf-8') as out_csv, open(csv_path, 'r', newline='',
                                                                            encoding='utf-8') as csv_file:
        reader = csv.reader(csv_file, delimiter=',')
        rows = list(reader)
        filtered_rows = [r for r in rows[1:] if r[-1] == '成功']
        writer = csv.writer(out_csv)
        writer.writerow(rows[0])
        writer.writerows(filtered_rows)


if __name__ == '__main__':
    # join_csv('E:\\工作\\客户-项目\\中兴\\数据导入第2轮\\业务数据包\\文档物理文件映射', 'res/文档物理文件映射.csv')
    filter_csv('csv/bo_Document0.csv', 'res/bo_Document0_succeed.csv')
