from django.http import HttpResponse
from django.core import serializers
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
        # who.increment_checkins()
        
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
                    "marker" : step['marker'],
					"polyline" : step['polyline']
                })                
        response['routes'].append(r)    
    
    badges = badge_handler(who, float(speed))
    response['badges'] = map(lambda badge: badge.json_format(), badges)
    
    return HttpResponse(json.dumps(response), mimetype="application/json")


"""
A Method that handle the pings coming from the device updating the info of the road taken by the user
    
@author Monayri
"""

def update(request, stepId, speed, who):
    myStep = Step.objects.get(pk=stepId)
    if myStep:
        Ping_Log(step = myStep, speed = speed, who = get_device(who), time = datetime.now()).save()
    return HttpResponse(json.dumps("Posted"), mimetype="application/json") 
    
    #Here i will check if the route is going to be blocked 
    
    #If yes i will search for another route
    #if no then i will send an empty response
    
### FOR TESTING PURPOSES,  ADD A VIEW THAT CALLS YOUR MODEL METHOD
##################################################################

# Testing to call model from view
def test_method_in_views(request, test_value):
    return HttpResponse(test_method_in_models(test_value))


def getTwitterLoginInfo(request, user_name):
    print user_name
    return HttpResponse(json.dumps(getLoginInfo(user_name)))

def checkUserExists(request, user_name):
    print user_name
    return HttpResponse(saveTwitterUserInfo(user_name,token,secret))

def saveTwitterUserInfo(request, user_name,token,secret):
    print user_name
    return HttpResponse(saveTwitterUserInfo(user_name,token,secret))

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


def rate_comment(request, who, comment_id, rate):
    """
    Rates this comment
    Comment is either 1, 2, 3:
    case 1: up vote
    case 2: down vote
    case 3: flag comment
    
    @author kamasheto
    """
    c = Comment.objects.get(pk = comment_id)
    voter = Device.objects.get(installation_id = who)
    rate = int(rate)
    if rate == 1: # up
        c.do_up(voter)
    elif rate == 2: # down
        c.do_down(voter)
    elif rate == 3: #flag
        c.do_flag(voter)
    
    return HttpResponse(json.dumps({"success" : 1}))

def get_comments(request, lat, lng, refresh_query):
    """
    Gets comments near this location. Comments near this location within 5mi are returned, alongside the future query for twitter in case udpates are needed
    
    @author kamasheto
    """
    refresh_query = refresh_query if refresh_query else None
    comments, query = get_comments_near(lat, lng, refresh_query)
    return HttpResponse(json.dumps({"comments" : comments, "query": query}))
