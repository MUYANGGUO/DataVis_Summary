import http.client
import json
import time
import timeit
import sys
import collections
from pygexf.gexf import *
import os


def hex_to_rgb(value):
    lv = len(value)
    return list(str(int(value[i:i + lv // 3], 16)) for i in range(0, lv, lv // 3))


dir = os.path.dirname(os.path.realpath(__file__))
api_key = sys.argv[1]

connection = http.client.HTTPConnection('www.rebrickable.com',timeout=80)

sets_api_call ='https://rebrickable.com/api/v3/lego/sets/?page=1&page_size=1000&min_parts=1140&ordering=num_parts&key='+api_key

connection.request("GET",sets_api_call)
response = connection.getresponse()
# print("Status: {} and reason: {}".format(response.status, response.reason))
data = response.read()
data = json.loads(data)
results = data['results']
count = data['count']
topset= []

for i in range(count):
    set_tuple = (results[i]['name'],results[i]['set_num'])

    topset.append(set_tuple)




all_sets_parts_selected = []

for single_set_tuple in topset:
    single_set_number = single_set_tuple[1]
    # print(single_set_tuple[1])
    parts_api_call = 'https://rebrickable.com/api/v3/lego/sets/'+single_set_number+'/parts/?page=1&page_size=1000&key='+api_key
    
    connection.request("GET",parts_api_call)
    response = connection.getresponse()

    parts_data = response.read()
    parts_data = json.loads(parts_data)
    next_call = parts_data['next']
    parts_result = parts_data['results']
    single_set_parts = []
    for part_result in parts_result:
        part_num_color = part_result['part']['part_num']+'_'+part_result['color']['rgb']
        parts_tuple = (part_result['part']['name'],part_num_color,part_result['quantity'])
        single_set_parts.append(parts_tuple)
    
    single_set_parts.sort(key=lambda tup: tup[2] ,reverse=True)
    if len(single_set_parts) >20:
        single_set_parts = single_set_parts[0:20]
    else:
        None
    all_sets_parts_selected.append(single_set_parts)

   
# implement your data retrieval code here
#


# complete auto grader functions for Q1.1.b,d
def min_parts():
    """
    Returns an integer value
    """
    # you must replace this with your own value
    return 1140

def lego_sets():
    """
    return a list of lego sets.
    this may be a list of any type of values
    but each value should represent one set

    e.g.,
    biggest_lego_sets = lego_sets()
    print(len(biggest_lego_sets))
    > 280
    e.g., len(my_sets)
    """
    # you must replace this line and return your own list
    # my_set = get_data(sys.argv[1])
    # my_list = my_set['set_name'][:][0]
    return topset
x = len(lego_sets())


def gexf_graph():
    """
    return the completed Gexf graph object
    """
    # you must replace these lines and supply your own graph
    my_gexf = Gexf("muyang guo", "bricks_graph")
    graph = my_gexf.addGraph("undirected", "static", "I'm an bricks graph")
    # graph.addNodeAttribute("Type","string")
    atr = graph.addNodeAttribute("Type","part","string")
    # atr = graph.attributes.declareAttribute("node","string","",title="Type", mode="static", id=None)
    
    
    for i in range(len(topset)):
        set_info = topset[i]
        set_id = str(set_info[1])
        set_name = str(set_info[0])
        if graph.nodeExists(set_id) ==0:
            set_nodes = graph.addNode(set_id,set_name)
            set_nodes.addAttribute(atr,"set")
            # Atr.declareAttribute("node","string","set",title="Type", mode="static", id=set_id)
            
            set_nodes.setColor("0","0","0")
        for parts_info in all_sets_parts_selected[i]:
            
            part_id = str(parts_info[1])
            part_name = str(parts_info[0])
            part_quantity = str(parts_info[2])
            if graph.nodeExists(part_id) ==0:
                parts_nodes = graph.addNode(part_id,part_name)
                parts_nodes.addAttribute(atr,"part")
                # Atr.declareAttribute("node","string","part",title="Type", mode="static", id=part_id)
                
                part_node_color_RGB = hex_to_rgb(part_id.split('_')[1])
                parts_nodes.setColor(part_node_color_RGB[0],part_node_color_RGB[1],part_node_color_RGB[2])
            edge_id = set_id+part_id
            edges = graph.addEdge(edge_id,set_id,part_id,part_quantity)
           
    with open(dir+'/'+"bricks_graph.gexf", "wb") as f:
        my_gexf.write(f)
    return my_gexf.graphs[0]

gexf_graph()

# complete auto-grader functions for Q1.2.d

def avg_node_degree():
    """
    hardcode and return the average node degree
    (run the function called “Average Degree”) within Gephi
    """
    # you must replace this value with the avg node degree
    return 5.24

def graph_diameter():
    """
    hardcode and return the diameter of the graph
    (run the function called “Network Diameter”) within Gephi
    """
    # you must replace this value with the graph diameter
    return 8

def avg_path_length():
    """
    hardcode and return the average path length
    (run the function called “Avg. Path Length”) within Gephi
    :return:
    """
    # you must replace this value with the avg path length
    return 4.417