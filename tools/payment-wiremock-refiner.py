import json
import os
import re
import sys
filename = sys.argv[1]
masking_string = 'HASHED:'
linechanger = '\n'
state = 'STATE'
state_count = 1
api_call_count = 1
white_list_headers = ['Accept', 'Content-Type', 'Date', 'x-fapi-financial-id', 'x-fapi-interaction-id' ]
parsed_content = []
parsing_phase = ''

def prepare_new_file_content(content):
  	if masking_string in content:
  	    parsed_content.append(content.replace(masking_string, 'MASKED'))
   	else:
   	    parsed_content.append(content)


def assign_set_match_state(line):
    #REQUET N / RESPONSE N
    pair = line.split(' ')
    #REQUEST / RESPONSE
    request_or_response = pair[0]
    if request_or_response == 'REQUEST':
        if api_call_count > 2:
            return 'REQUEST ' + str(api_call_count) + ' MATCH ' + state + str(state_count) + linechanger
        else:
            return 'REQUEST ' + str(api_call_count) + linechanger
    elif request_or_response == 'RESPONSE':
        if api_call_count > 1:
            return 'RESPONSE ' + str(api_call_count) + ' SET ' + state + str(state_count) + linechanger
        else:
            return 'RESPONSE ' + str(api_call_count) + linechanger
    else:
        return line

with open(filename) as f:
    for line in f:
        #REQUEST and giving a STATE
        if line.startswith('REQUEST'):
            prepare_new_file_content(assign_set_match_state(line))
            state_count = state_count + 1
        #RESPONSE and giving a STATE
        elif line.startswith('RESPONSE'):
            prepare_new_file_content(assign_set_match_state(line))
            api_call_count = api_call_count + 1
        #Empty Line
        elif line.rstrip() == '':
                prepare_new_file_content(line)
                if parsing_phase == 'HEADER':
                    parsing_phase = 'PAYLOAD'
        #HTTP Method and Url
        elif line.startswith('GET') or line.startswith('POST') or line.startswith('PUT') or line.startswith('DELETE'):
                prepare_new_file_content(line)
                parsing_phase = 'HEADER'
        #HTTP RESPONSE
        elif re.match('[0-9][0-9][0-9]',line.rstrip()):
                prepare_new_file_content(line)
                parsing_phase = 'HEADER'
        #JSON PAYLOAD REQUEST and RESPONSE
        elif line.startswith('{'):
            try:
                asJson = json.loads(line)
            except ValueError as e:
                prepare_new_file_content(line)
            else:
                prepare_new_file_content(json.dumps(asJson, indent=4, sort_keys=True) + linechanger)
        #Form PAYLOAD
        elif '=' in line and '&' in line and not ('/' in line):
            prepare_new_file_content(line)
        elif parsing_phase == 'PAYLOAD':
            prepare_new_file_content(line)
        #HEADERS Pattern
        elif line.split(":")[0] in white_list_headers:
            prepare_new_file_content(line)
        else:
            print 'Omitting ', line


print 'Only Keep the Headers Below'
print(white_list_headers)
print 'To add extra headers please add manually at white_list_headers in this py script'
print 'Input AAP File:', sys.argv[1]
head, tail = os.path.split(sys.argv[1])
outPutFilename = head + '/parsed_' + tail
print 'Output AAP File:', outPutFilename
f = open(outPutFilename, "w")
for x in parsed_content:
    f.write(x)
f.close()

