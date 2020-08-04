# Updating test.yml

To add dummy secrets for all providers automatically into test.yml (this is required for some tests to succeed) you can execute *"dummy_values_dict.py"*.

To run this script, you need to install PyYaml dependency which can be installed by:

```
pip install PyYaml
```

After installing the dependency, you can run the script by executing

```
python etc/dummy_secrets_adder.py
```
