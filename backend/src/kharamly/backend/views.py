from django.http import HttpResponse
from kharamly.backend.models import *
from datetime import datetime
import urllib, json

"""
The API call. This is optimized for frequent calls in the form:
    http://url/to/action/30.091538,31.31633/29.985067,31.43873/10/android_id
    30.091538,31.31633 - from 
    29.994192,31.444588 - to (GUC location)
    10 - speed
    android_id - Installation device ID
    
@author kamasheto && Monayri
"""
def api(request, orig, dest, speed, who):
    orig = orig.split(",")
    dest = dest.split(",")
    from_node = get_node(orig[0], orig[1])
    to_node = get_node(dest[0], dest[1])
    who = get_device(who)
    # my_step could be kept tracked of, but what about sending it untracked everytime?
    # my_step = None
    my_step = get_step_from_node(from_node)
    if my_step:
        Ping_Log(step=my_step, speed=speed, who=who, time=datetime.now(), persistence=get_persistence(who)).save()
        who.increment_checkins()
        
    result = getalternatives(None, my_step, to_node, from_node)
    response  = {"routes": []}
    for route in result['routes']:
        r= {"steps":[]}
        for leg in route['legs']:
            for step in leg['steps']:
                print leg['steps']
                print step['loc']
                r['steps'].append({
                    "s_lat": step['start_location']['lat'],
                    "s_lng": step['start_location']['lng'],
                    "e_lat": step['end_location']['lat'],
                    "e_lng": step['end_location']['lng'],
                    "col": get_color_from_speed(step['speed']),
                    "loc" : step['loc'],
                    "marker" : step['marker']
                })                
        response['routes'].append(r)    
    
    badges = badge_handler(who, float(speed))
    response['badges'] = map(lambda badge: badge.json_format(), badges)
    
    return HttpResponse(json.dumps(response), mimetype="application/json")


"""
A Method that handle the pings coming from the device updating the info of the road taken by the user
    
@author Monayri
"""

def update(stepId, routeId, speed, who):
    myStep = Step.objects.get(pk=stepId)
    myRoute = Route.objects.get(pk=routeId)
    if myStep:
        Ping_Log(step = myStep, speed = speed, who = get_device(who), time = datetime.now()).save()
    
    #Here i will check if the route is going to be blocked 
    
    #If yes i will search for another route
    #if no then i will send an empty response
    
### FOR TESTING PURPOSES,  ADD A VIEW THAT CALLS YOUR MODEL METHOD
##################################################################

def inRadius(request,longitude,latitude,radius):
    print 'hi'
   
    return HttpResponse(get_steps_around(float(longitude), float(latitude),float(radius)))



def route_blockage(request, origin, destination):
	#url = 'http://maps.googleapis.com/maps/api/directions/json?origin=' + origin + '&destination=' + destination + '&sensor=true&alternatives=true'
	#result = json.load(urllib.urlopen(url))
	#getdirections(origin, destination, result)
	#return json.dumps(updateResult(result))
	#return HttpResponse(json.dumps(updateResult(result)))
	A=datetime.now()
	B=datetime.now()+timedelta(minutes=30)
	timeDiff = B-A
	print timeDiff
 	days = timeDiff.days*24*60
 	hours = timeDiff.seconds*3600
  	minutes = timeDiff.seconds*60
  	seconds = timeDiff.seconds
	
	return HttpResponse(seconds+minutes+hours+days)

	#return HttpResponse(evaluate(Node.objects.get(id=1), Node.objects.get(id=3), Leg.objects.get(id=1),0, Step.objects.get(id=1)))

def directions(request, origin, destination):
    return HttpResponse(json.dumps(getdirections(origin, destination)))
    
def alternatives(request, location, destination):
    return HttpResponse(getalternatives(location, destination))





####################################################################
# BEGIN kama's sandbox
# DO NOT COME ANYWHERE NEAR.
####################################################################

def rate_comment(request, comment_id, rate):
    return HttpResponse("Hello world")



####################################################################
# END kama's sandbox
# NOW you can play around.
####################################################################
