from django.db import models
from datetime import datetime
# from decimal import *

# Create your models here.
# testing git reset --hard
class Node(models.Model):
    latitude = models.FloatField()
    longitude = models.FloatField()

    def __unicode__(self):
        return str(self.id) + ":" + str(self.latitude) + ", " + str(self.longitude)

class Step(models.Model):
    html_instructions = models.TextField()
    distance_text = models.CharField(max_length=200)
    distance_value = models.IntegerField()
    duration_text = models.CharField(max_length=200)
    duration_value = models.IntegerField()
    start_location = models.ForeignKey(Node, related_name='start')
    end_location = models.ForeignKey(Node, related_name='end')

    def __unicode__(self):
        return str(self.start_location.id) + ", " + str(self.end_location.id)

class Step_History(models.Model):
    step = models.ForeignKey(Step, related_name='current_step')
    time = models.DateTimeField()
    speed = models.FloatField()

    def __unicode__(self):
        return str(self.step)+","+str(self.time)

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
	
    def __unicode__(self):
        return str(self.start_location) + "," + str(self.end_location)
		    

class Route(models.Model):
    summary = models.CharField(max_length=200)
    legs = models.ManyToManyField(Leg)
	
    def __unicode__(self):
        return self.summary
    
    
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

def getdirections(origin, destination, result):
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
    return routes if len(routes) > 1 else None

# Author : Ahmed Abouraya
# takes a JSONObject and updates all steps speeds with the information in the database
def updateResult(result):
	routes = result['routes']
	for route in routes :
		summ = route['summary']
		legs = route['legs']

		for leg in legs :
			distance_text = leg['distance']['text']
			distance_value = leg['distance']['value']
			duration_text = leg['duration']['text']
			duration_value = leg['duration']['value']
			start_address = leg['start_address']
			end_address = leg['end_address']
			start_loc = leg['start_location']
			end_loc = leg['end_location']
			steps = leg['steps']
			for step in steps:
				html = step['html_instructions']
				distance_text = step['distance']['text']
				distance_value = step['distance']['value']
				duration_text = step['duration']['text']
				duration_value = step['duration']['value']
				current_start_location = step['start_location']
				current_end_location = step['end_location']
				stepHistoryList = Step_History.objects.filter(step__start_location__latitude=current_start_location['lat'],
				                    step__start_location__longitude=current_start_location['lng'],
				                    step__end_location__latitude=current_end_location['lat'],
				                    step__end_location__longitude=current_end_location['lng'])[:5]
				counter=0
				avgSpeed=0
				for s in stepHistoryList.all():
					counter=counter+1
					avgSpeed=avgSpeed+s.speed
				if counter==0:
					step['speed']=-1
				else:				
					avgSpeed=avgSpeed/counter
					step['speed']=avgSpeed
	return result

# calculates distance between two nodes
def getDistance(current,target):
	lat = current['lat'] / 1E6 - target['lat']  / 1E6;
	lng = current['lng']  / 1E6 - target['lng']  / 1E6;
	return math.sqrt(lat*lat+lng*lng)
	
# checks if the current road is blocked,if so it updates the database
# loops over all steps, when the currentStep is reached, checks whether the driver has reached the end of the step or not if yes insert information in database
# checks for future steps if they're blocked if yes checks fro alternatives
def evaluate(origin, destination, result, speed, currentStep, startTime):
	routes = result['routes']
	for route in routes :
		summ = route['summary']
		legs = route['legs']

		for leg in legs :
			distance_text = leg['distance']['text']
			distance_value = leg['distance']['value']
			duration_text = leg['duration']['text']
			duration_value = leg['duration']['value']
			start_address = leg['start_address']
			end_address = leg['end_address']
			start_loc = leg['start_location']
			end_loc = leg['end_location']
			steps = leg['steps']

			flag=True
			#check if speed is 0 insert current step as blocked
			if blockedRoad(speed):
				currentStepHistory = Step_History(step = CurrentStep,time=datetime.now(),speed=0)
				currentStepHistory.save()

			for step in steps:
				if step==currentStep:
				#if current step is not reached check if the user has reached it's end
					flag=False
					if getDistance(origin,currentStep['end_location'])<0.0002 :
						currentStepHistory = Step_History(step = currentStep,time=datetime.now(),
						                            speed=(startTime-datetime.now())/currentStep['distance']['value'])
					currentStepHistory.save()
				
				if flag :
				#if currentStep is not reached skip
					continue

				#if currentStep is reached check if a future step is blocked

				html = step['html_instructions']
				distance_text = step['distance']['text']
				distance_value = step['distance']['value']
				duration_text = step['duration']['text']
				duration_value = step['duration']['value']
				current_start_location = step['start_location']
				current_end_location = step['end_location']
	
				stepHistoryLists=Step_History.objects.filter(step__start_location__latitude=current_start_location['lat'],
				                                        step__start_location__longitude=current_start_location['lng'],
				                                        step__end_location__latitude=current_end_location['lat'],
				                                        step__end_location__longitude=current_end_location['lng'])[:5]
				counter=0
				for s in stepHistoryLists.all():
					if blockedRoad(s.speed):
						counter=counter+1
				if counter>0:
				#request for alternatives
					return updateResult(getalternatives(origin, destination))
		return updateResult(result)

# used for testing			 
def test_evaluate(origin, destination,leg,speed,CurrentStep):
	steps = leg.steps
	flag=True
	#check if speed is 0 insert current step as blocked
	if blockedRoad(speed):
		currentStepHistory = Step_History(step = CurrentStep,time=datetime.now(),speed=0)
		currentStepHistory.save()
	#insert current step as blocked
	for curStep in steps.all():
		#if currentStep is not reached skip
		if curStep==CurrentStep:
			flag=False
		else:
			continue
		#if currentStep is reached check if a future step is blocked
		
		#html = step['html_instructions']
		#distance_text = step['distance']['text']
		#distance_value = step['distance']['value']
		#duration_text = step['duration']['text']
		#duration_value = step['duration']['value']
		current_start_location = curStep.start_location
		current_end_location = curStep.end_location

		stepHistoryLists=Step_History.objects.filter(step__start_location=current_start_location)[:5]
		counter=0
		for s in stepHistoryLists.all():
			if blockedRoad(s.speed):
				counter=counter+1
		if counter>0:
		#request for alternatives
			#return getalternatives(origin, destination)
			return True
	return False
	
#determines whether a road is blocked or not
def blockedRoad(speed):
	return speed == 0
