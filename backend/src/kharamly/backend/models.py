from django.db import models

# Create your models here.
# testing git reset --hard
class Node(models.Model):
    latitude = models.DecimalField(verbose_name='latitude', name='latitude', max_digits=11, decimal_places=9)
    longitude = models.DecimalField(verbose_name='longitude', name='longitude', max_digits=12, decimal_places=9)

class Step(models.Model):
    html_instructions = models.TextField()
    distance_text = models.CharField(max_length=200)
    distance_value = models.IntegerField();
    duration_text = models.CharField(max_length=200)
    duration_value = models.IntegerField();
    start_location = models.ForeignKey(Node, related_name='start')
    end_location = models.ForeignKey(Node, related_name='end')


class Leg(models.Model):
    steps = models.ManyToManyField(Step)
    distance_text = models.CharField(max_length=200)
    distance_value = models.IntegerField()
    duration_text = models.CharField(max_length=200)
    duration_value = models.IntegerField()
    start_address = models.TextField()
    end_address = models.TextField()
    start_location = models.ForeignKey(Node, related_name='start_node')
    end_location = models.ForeignKey(Node, related_name='end_node')
    
class Route(models.Model):
    summary = models.CharField(max_length=200)
    legs = models.ManyToManyField(Leg)
    
    
#####################################################################
## BUSINESS LOGIC
## IN PYTHON, IT IS ADVISED TO KEEP LOGIC IN THE MODELS
#####################################################################
#####################################################################

# Test method in model
def test_method_in_models(num):
    return num * 2

# Author : Moataz Mekki
# <sensor> & <alternatives> take the value true or false only
# <origin> & <destination> can be address or long & lat

def getdirections(origin, destination, sensor, alternatives, result):
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
            start_loc = leg['start_location']
            end_loc = leg['end_location']
            s_node = Node(latitude = start_loc['lat'], 
                              longitude = start_loc['lng'])
            s_node.save()
            e_node = Node(latitude = end_loc['lat'], 
                            longitude = end_loc['lng'])
            e_node.save()
            steps = leg['steps']
            current_leg = Leg(duration_text = duration_text, 
                              duration_value = duration_value, 
                              distance_text = distance_text, 
                              distance_value = distance_value, 
                              start_address = start_address, 
                              end_address = end_address,
                              start_location = s_node, 
                              end_location = e_node)
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
    return result

#@author: Monayri
#@param Location: The start Node id of the step the user currently at
#@param Destination: The Destination of the user in the form of a node id
#@return: A JSON object containting the alternative route(s)
def getalternatives(location, destination):
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
    # return len(routes) > 1 ? routes : None
    return routes if len(routes) > 1 else None
