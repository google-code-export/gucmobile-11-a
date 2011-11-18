from django.http import HttpResponse
from kharamly.backend.models import *
import urllib, json

# For now this will return dummie results, for the frontend to process and visualize
# Later versions will trigger and use the proper actions defined by others
def api(request, lng, lat, who):
    # todo
    # send more routes?
    response = {"steps" : 
            [{"s_lng": 31.24906000000001, "s_lat": 30.065440, "e_lng": 31.256110, "e_lat": 30.099050, "col": 1},
            {"s_lng": 31.256110, "s_lat": 30.099050, "e_lng": 31.255410, "e_lat": 30.105590, "col": 2},
            {"s_lng": 31.255410, "s_lat": 30.105590, "e_lng": 31.252130, "e_lat": 30.113050, "col": 3},
            {"s_lng": 31.252130, "s_lat": 30.113050, "e_lng": 31.243610, "e_lat": 30.12307000000001, "col": 120},
            {"s_lng": 31.243610, "s_lat": 30.12307000000001, "e_lng": 31.236110, "e_lat": 30.132170, "col": 60}]
    }
    return HttpResponse(json.dumps(response), mimetype="application/json")
    
### FOR TESTING PURPOSES,  ADD A VIEW THAT CALLS YOUR MODEL METHOD
##################################################################

# Testing to call model from view
def test_method_in_views(request, test_value):
    return HttpResponse(test_method_in_models(test_value))

def route_blockage(request):
	return HttpResponse(test_evaluate(Node.objects.get(id=1), Node.objects.get(id=3), Leg.objects.get(id=1),0, Step.objects.get(id=1)))

def directions(request, origin, destination, sensor, alternatives):
    url = 'http://maps.googleapis.com/maps/api/directions/json?origin=' + origin + '&destination=' + destination + '&sensor=' + sensor + '&alternatives=' + alternatives
    result = json.load(urllib.urlopen(url))
    return HttpResponse(json.dumps(getdirections(origin, destination, sensor, alternatives, result)))
    
def alternatives(request, location, destination):
    return HttpResponse(getalternatives(location, destination))
