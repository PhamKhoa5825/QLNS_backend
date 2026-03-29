import os
import re
import glob

controller_dir = r'c:\Users\keduy\Desktop\QLNS_all\QLNS_backend\src\main\java\com\example\qlns\Controller'
java_files = glob.glob(os.path.join(controller_dir, '*.java'))

path_var_pattern = re.compile(r'@PathVariable\s+([\w<>]+)\s+(\w+)')
req_param_pattern = re.compile(r'@RequestParam\s+([\w<>]+)\s+(\w+)')

for file in java_files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = path_var_pattern.sub(lambda m: f'@PathVariable(\"{m.group(2)}\") {m.group(1)} {m.group(2)}', content)
    new_content = req_param_pattern.sub(lambda m: f'@RequestParam(\"{m.group(2)}\") {m.group(1)} {m.group(2)}', new_content)

    if new_content != content:
        with open(file, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f'Fixed {os.path.basename(file)}')
