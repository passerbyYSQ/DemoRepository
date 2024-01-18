import os
import shutil


def copy_files(file_names, destination_folder_name):
    current_directory = os.getcwd()
    destination_folder_path = os.path.join(current_directory, destination_folder_name)
    os.makedirs(destination_folder_path, exist_ok=True)

    files_to_copy = []

    for root, dirs, files in os.walk(current_directory):
        if 'copy' in dirs:
            dirs.remove('copy')
        for file_name in files:
            files_to_copy.append(os.path.join(root, file_name))
            if file_name.replace(" ", "") in file_names:
                print('Copying ' + os.path.join(root, file_name))
                source_path = os.path.join(root, file_name)
                destination_path = os.path.join(destination_folder_path, file_name)
                shutil.copy(source_path, destination_path)
                print('Copied ' + os.path.join(root, file_name))
                print('--' * 50)
    with open('files_to_copy.txt', 'w', encoding='utf-8') as file:
        file.writelines("\n".join(files_to_copy))


if __name__ == '__main__':
    file_names = []
    with open('file_name.txt', 'r', encoding='utf-8') as file:
        lines = file.readlines()
        for line in lines:
            line = line.replace(" ", "")
            file_names.append(line.strip())

    copy_files(file_names, 'copy')

    print('copy finished')

    while True:
        pass
