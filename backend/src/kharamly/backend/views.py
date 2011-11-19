from django.http import HttpResponse
from kharamly.backend.models import *
from datetime import datetime
import urllib, json

"""
@author kamasheto
The API call. This is optimized for frequent calls in the form:
    http://url/to/action/30.091538,31.31633/29.985067,31.43873/10/android_id
    30.091538,31.31633 - from 
    29.994192,31.444588 - to (GUC location)
    10 - speed
    android_id - Installation device ID
"""
def api(request, orig, dest, speed, who):
    orig = orig.split(",")
    dest = dest.split(",")
    from_node = get_node(orig[0], orig[1])
    to_node = get_node(dest[0], dest[1])
    # my_step could be kept tracked of, but what about sending it untracked everytime?
    # my_step = None
    my_step = get_step_from_node(from_node)
    if my_step:
        Ping_Log(step = my_step, speed = speed, who = get_device(who), time = datetime.now()).save()
    result = getalternatives(None, my_step, to_node, from_node)
    routes = evaluate_route(result, speed, my_step)
    response  = {"steps": []}
    for route in routes:
        for leg in route['legs']:
            for step in leg['steps']:
                response['steps'].append({
                    "s_lat": step['start_location']['lat'],
                    "s_lng": step['start_location']['lng'],
                    "e_lat": step['end_location']['lat'],
                    "e_lng": step['end_location']['lng'],
                    "col": get_color_from_speed(step['speed']),
                })
    return HttpResponse(json.dumps(response), mimetype="application/json")
    
### FOR TESTING PURPOSES,  ADD A VIEW THAT CALLS YOUR MODEL METHOD
##################################################################

# Testing to call model from view
def test_method_in_views(request, test_value):
    return HttpResponse(test_method_in_models(test_value))

def route_blockage(request):
	return HttpResponse(test_evaluate(Node.objects.get(id=1), Node.objects.get(id=3), Leg.objects.get(id=1),0, Step.objects.get(id=1)))

def directions(request, origin, destination):
    return HttpResponse(json.dumps(getdirections(origin, destination)))
    
def alternatives(request, location, destination):
    return HttpResponse(getalternatives(location, destination))