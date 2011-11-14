# Create your views here.
from django.http import HttpResponse
import urllib, json
from backend.models import Node, Step, Leg, Route
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
