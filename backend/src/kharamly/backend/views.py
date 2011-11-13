# Create your views here.
from django.http import HttpResponse
import urllib, json

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
    origin = origin.replace('-', '.')
    origin = origin.replace('_', ',')
    destination = destination.replace('-', '.')
    destination = destination.replace('_', ',')
    url = 'http://maps.googleapis.com/maps/api/directions/json?origin='+origin+'&destination='+destination+'&sensor='+sensor+'&alternatives='+alternatives
    result = json.load(urllib.urlopen(url))
    return HttpResponse(json.dumps(result), mimetype="application/json")
