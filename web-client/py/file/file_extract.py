import collections
import multiprocessing
import os
import shutil

"""
1.命令行执行以下命令 生成exe文件
pyinstaller -F file_extract.py

2.准备file_name.txt文件
“
D:\export\Physicalfile\CAD Drawing\CAD Drawing5\PhysicalFile\\6605BEVB1-5101400-M018雷达支架焊接图(1)20210406HA1.pdf
00148558-f578-485c-80ba-abead8bcaec5
”
第一行为文件的实际位置
第二行为中兴环境需要的的文件名, 因默认按文件都处于仓库，所以文件名是文件对象的UUID

3.处理结果

3.1 提取的文件会在copy\\ZXVault 文件夹下,将ZXVault该文件夹拷贝到zx plm服务器的相应位置
3.2 其他结果 重点关注 file_no_exist.txt不存在的文件 可能会因为特殊字符而无法找到相关文件 如 "Φ"无法识别,此时需要手工去找到该文件 并将该文件重命名为相应的UUID,并置于copy\\ZXVault 文件夹下
生成3个文件:
_file_no_exist.txt 不存在的文件
_file_mapping.txt: 文件映射信息
file_phys.txt:所有的文件信息
"""

def copy_files(file_names, destination_folder_name, copy, prefix):
    current_directory = os.getcwd()
    destination_folder_path = os.path.join(current_directory, destination_folder_name)
    os.makedirs(destination_folder_path, exist_ok=True)

    file_no_exist = []

    file_mapping = {}

    file_phys = []

    size = 0

    file_paths = collections.defaultdict(list)
    for root, dirs, files in os.walk(os.getcwd()):
        for file in files:
            file_paths[os.path.basename(file).replace(" ", "").replace("\u3000", "")].append(os.path.join(root, file))
            file_phys.append(os.path.join(root, file))

    for file_name, new_file_name in file_names:
        print(file_name)
        k = file_name
        if prefix:
            file_name = file_name.replace(prefix[0], prefix[1])

        destination_file = os.path.join(os.getcwd(), destination_folder_name, 'ZXVault', new_file_name)
        # Get the parent directory of the file
        parent_folder = os.path.dirname(destination_file)

        # Create the parent directory
        os.makedirs(parent_folder, exist_ok=True)

        if os.path.exists(file_name):
            file_mapping[k] = file_name
            size += os.path.getsize(file_name)
            if copy:
                shutil.copy2(file_name, destination_file)

            continue

        file_name_list = file_name.split('\\')
        file_name_list[-1] = file_name_list[-1].replace(" ", "").replace("\u3000", "")
        if file_name_list[-1] not in file_paths:
            file_no_exist.append(file_name)
            file_no_exist.append(new_file_name)
            continue

        file_name1 = '\\'.join(file_name_list[0:-1])
        file_name2 = ''
        for fn in file_paths[file_name_list[-1]]:
            if fn.startswith(file_name1):
                file_name2 = fn
                break
        if file_name2:
            file_mapping[k] = file_name2
            size += os.path.getsize(file_name2)
            if copy:
                shutil.copy2(file_name2, destination_file)

            continue

        file_no_exist.append(file_name)
        file_no_exist.append(new_file_name)

    current_process = multiprocessing.current_process()
    process_id = str(current_process.pid)
    with open(process_id + '_file_no_exist.txt', 'w', encoding='utf-8') as file:
        for file_name in file_no_exist:
            file.write(file_name + '\n')

    with open(process_id + '_file_mapping.txt', 'w', encoding='utf-8') as file:
        for k, v in file_mapping.items():
            if k == v:
                continue
            file.write(k + '::::' + v + '\n')

    with open('file_phys.txt', 'w', encoding='utf-8') as file:
        file.writelines('\n'.join(file_phys))

    print(size)


if __name__ == '__main__':

    copy_flag = True
    prefix = ()
    # 配置文件
    # copy=1  是否copy文件 1 拷贝文件 其他 不拷贝文件
    # D:\export=D:\export12  替换文件位置 映射
    with open('config.txt', 'r', encoding='utf-8') as file:
        lines = file.readlines()
        line = lines[0].strip()
        v = line.split("=")[1]
        if v != '1':
            copy_flag = False
        prefix = (lines[1].strip().split("=")[0], lines[1].strip().split("=")[1])

    file_names = []
    with open('file_name.txt', 'r', encoding='utf-8') as file:
        lines = file.readlines()
        for i in range(0, len(lines), 2):
            file_names.append((lines[i].strip(), lines[i + 1].strip()))

    copy_files(file_names, 'copy', copy_flag, prefix)

    print('copy finished')

    while True:
        pass
