class SupplementalStdin(object):
    def __init__(self, field_dicts):
        self.answers = {}
        fields = [Field(field_dict) for field_dict in field_dicts]

        print("-" * 20)

        for field in fields:
            key = field.name
            desc = field.get_description()

            if field.value:
                value = field.value
                print(f"{desc}: {value}")
                input("Press enter to continue.")

            elif field.select_options:
                print(desc)

                option_values_by_index = {}
                option_index = 0

                for option in field.select_options:
                    print(f"({option_index}) {option.text}")

                    option_values_by_index[option_index] = option.value
                    option_index += 1

                print("---")
                chosen_index = int(input("Select index of option that you want to choose: "))
                value = option_values_by_index[chosen_index]

            else:
                value = input(f"{desc}: ")

            self.answers[key] = value

        print("-" * 20)

    def get_answers(self):
        return self.answers


class Field:

    def __init__(self, properties_dict):
        self.name = properties_dict.get("name")
        self.value = properties_dict.get("value")

        self.description = properties_dict.get("description")
        self.help_text = properties_dict.get("helpText")
        self.hint = properties_dict.get("hint")

        self.select_options = self.create_select_options(properties_dict.get("selectOptions"))

    @staticmethod
    def create_select_options(select_option_dicts):
        if select_option_dicts is None or len(select_option_dicts) == 0:
            return []

        return [SelectOption(select_option_dict) for select_option_dict in select_option_dicts]

    def get_description(self):
        desc = ''
        if self.description and self.help_text:
            desc = self.description + '\n' + self.help_text
        elif self.description:
            desc = self.description
        elif self.help_text:
            desc = self.help_text
        return desc


class SelectOption:

    def __init__(self, properties_dict):
        self.text = properties_dict.get("text")
        self.value = properties_dict.get("value")

    def __repr__(self):
        return f"({self.value}) {self.text}"
