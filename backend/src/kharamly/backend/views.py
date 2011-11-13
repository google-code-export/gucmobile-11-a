# Create your views here.
from django.http import HttpResponse
import urllib, json

# @author kamasheto
# For now this will return dummie results, for the frontend to process and visualize
# Later versions will trigger and use the proper actions defined by others
def api(request, long, lat, who):
    # todo
    # save logs
    pass
    
    

# Author : Moataz Mekki
# the convention in the url is
# replace all the dots '.' in all decimals with dash '-'
# replace all the commas ',' in all decimals with dash '_'
# these chars are forbidden in a django url
# the url should be .../getdirections/<origin>/<destination>/<sensor>/<alternatives>
# <sensor> & <alternatives> take the value true or false only
# <origin> & <destination> can be address or long & lat
# don't forget to replace the forbidden chars
	
def getdirections(request, origin, destination, sensor, alternatives):
    origin = origin.replace('-', '.').replace('_', ',')
    # origin = origin.replace('_', ',')
    destination = destination.replace('-', '.').replace('_', ',')
    # destination = destination.replace('_', ',')
    
    # print origin, destination
    
    # Just checking if this would work ^k
    # origin = get_original_param(origin)
    # destination = get_original_param(destination)
    url = 'http://maps.googleapis.com/maps/api/directions/json?origin=' + origin + '&destination=' + destination + '&sensor=' + sensor + '&alternatives=' + alternatives
    result = json.load(urllib.urlopen(url))
    return HttpResponse(json.dumps(result), mimetype="application/json")

# Testing playing around with methods in the views file ^k
# def get_original_param(orig):
    # return orig.replace('-', '.').replace('_', ',')
