from django.http import HttpResponse
from kharamly.backend.models import *
from datetime import datetime
import urllib, json

def api(request, from, to, speed, who):
    
    from = from.split(",")
    to = to.split(",")
    to_node = get_node(to[0], to[1])
    from_node = get_node(from[0], from[1])
    # my_step could be kept tracked of, but what about sending it untracked everytime?
    # my_step = get_step_from_node(from_node)
    my_step = None
    result = getalternatives(None, my_step, to_node, from_node)
    result = evaluate()
    Ping_Log(step = my_step, speed = speed, who = who, time = datetime.now()).save()
    response  = {"steps": []}
    for route : result['routes']
        for leg : route['legs']
            for step : leg['steps']
                response['steps'].append({
                    "s_lat": step.start_location.latitude,
                    "s_lng": step.start_location.longitude,
                    "e_lat": step.end_location.latitude,
                    "e_lng": step.end_location.longitude,
                    "col": step
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