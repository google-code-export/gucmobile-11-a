# Create your views here.
from ctypes import util
from django.conf import settings
from django.core import serializers
from django.http import HttpResponse
from kharamly.backend.models import Node, Step, Leg, Route
import json
from django.utils import simplejson as json2
import urllib



# @author kamasheto
# For now this will return dummie results, for the frontend to process and visualize
# Later versions will trigger and use the proper actions defined by others
def api(request, long, lat, who):
    # todo
    # save logs
    # send more routes?
    response = {"steps" : 
            [{"s_lng": 31.24906000000001, "s_lat": 30.065440, "e_lng": 31.256110, "e_lat": 30.099050, "col": 1},
            {"s_lng": 31.256110, "s_lat": 30.099050, "e_lng": 31.255410, "e_lat": 30.105590, "col": 2},
            {"s_lng": 31.255410, "s_lat": 30.105590, "e_lng": 31.252130, "e_lat": 30.113050, "col": 3},
            {"s_lng": 31.252130, "s_lat": 30.113050, "e_lng": 31.243610, "e_lat": 30.12307000000001, "col": 120},
            {"s_lng": 31.243610, "s_lat": 30.12307000000001, "e_lng": 31.236110, "e_lat": 30.132170, "col": 60}]
    }
    return HttpResponse(json.dumps(response), mimetype="application/json")
    
    

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
<<<<<<< HEAD
    return HttpResponse(json.dumps(result), mimetype="application/json")
=======
    routes = result['routes']
    for route in routes :
        summ = route['summary']
        legs = route['legs']
        current_route = Route(summary = summ)
        current_route.save()
        for leg in legs :
            distance_text = leg['distance']['text']
            distance_value = leg['distance']['value']
            duration_text = leg['duration']['text']
            duration_value = leg['duration']['value']
            start_address = leg['start_address']
            end_address = leg['end_address']
            steps = leg['steps']
            current_leg = Leg(duration_text = duration_text, 
                              duration_value = duration_value, 
                              distance_text = distance_text, 
                              distance_value = distance_value, 
                              start_address = start_address, 
                              end_address = end_address)
            current_leg.save()
            for step in steps:
                html = step['html_instructions']
                distance_text = step['distance']['text']
                distance_value = step['distance']['value']
                duration_text = step['duration']['text']
                duration_value = step['duration']['value']
                start_location = step['start_location']
                end_location = step['end_location']
                start_node = Node(latitude = start_location['lat'], 
                                  longitude = start_location['lng'])
                start_node.save()
                end_node = Node(latitude = end_location['lat'], 
                                longitude = end_location['lng'])
                end_node.save()
                current_step = Step(html_instructions = html,
                                     duration_text = duration_text, 
                                     duration_value = duration_value, 
                                     distance_text = distance_text, 
                                     distance_value = distance_value, 
                                     start_location = start_node, 
                                     end_location = end_node)
                current_step.save()
                current_leg.steps.add(current_step)
                current_leg.save()
            current_route.legs.add(current_leg)
            current_route.save()
    
    return HttpResponse(json.dumps(result), mimetype="application/json")

# Testing playing around with methods in the views file ^k
# def get_original_param(orig):
    # return orig.replace('-', '.').replace('_', ',')



#@author: Monayri
#@param Location: The start Node id of the step the user currently at
#@param Destination: The Destination of the user in the form of a node id
#@return: A JSON object containting the alternative route(s)
def getalternative (request, location, destination):
    #First i will check if the alternative can be fetched from the database
    startNode = Node.objects.get(id = location)
    endNode = Node.objects.get(id = destination)
    startStep = Step.objects.filter(start_location = location)
    endStep = Step.objects.filter(end_location = destination)
    print endNode.longitude
    legs = Leg.objects.all()
    routes = []
    if(startStep != None and endStep!= None):
        print startStep
        print endStep
        for leg in legs :
            data = leg.steps.all()
            current_steps = []
            for cstep in data:
                current_steps.append(cstep)
            for step in startStep :
                for step2 in endStep : 
                    if step in current_steps:
                        if step2 in current_steps:
                            routeSummary = "" # Should Contain the route summary
                            currentRoute = Route(summary = routeSummary)
                            currentRoute.save()
                            current_leg = Leg(duration_text = "", 
                              duration_value = 1, 
                              distance_text = "", 
                              distance_value = 1, 
                              start_address = "longitude:" + str(startNode.longitude) + "latitude: " + str(startNode.latitude), 
                              end_address = "longitude:" + str(endNode.longitude) + "latitude: " + str(endNode.latitude))
                            current_leg.save()
                            for x in range(current_steps.index(step), current_steps.index(step2)):
                                current_leg.steps.add(current_steps[x])
                                current_steps[x].save()
                            current_leg.save()
                            currentRoute.legs.add(current_leg)
                            currentRoute.save()
                            routes.append(currentRoute)
    if(len(routes) > 1):
        return_data = serializers.serialize("json", routes)
        return HttpResponse(json.dumps(routes, default=encode_route), mimetype="application/json")
    
    
    return 

def encode_route(obj):
    if isinstance(obj, Route): 
        myString = "{ Summary:"+ obj.summary  + ", legs: ["      
        data = []
        for leg in obj.legs.all():    
            myString +=  "{steps: ["
            for step in leg.steps.all():
                myString += "{start_location : { longitude: "  + str(step.start_location.longitude) + ", latitude : "+ str(step.start_location.longitude)+"}"
                myString += ", end_location : { longitude : "  + str(step.end_location.longitude) + ", latitude : "+ str(step.end_location.longitude)+"}}"
                
            myString+="]}"
        myString+="]}"  
        return myString
     
>>>>>>> ef5491e359c7dd7272af5eeb0ad3440b1fe44b05
