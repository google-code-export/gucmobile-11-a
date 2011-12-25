from datetime import *
from random import *
from django.db import models
import urllib, json, math


# A entity of users, for later usage
class Device(models.Model):
    installation_id = models.CharField(max_length = 64)
    number_of_checkins = models.IntegerField(default=0)
    
    def has_badge(self, badge):
        """
        Return: 
            True if the user has acquired the badge, False otherwise
        Arguments:
            badge: Any badge object
        Author: Shanab
        """
        return badge in self.badge_set.all()
        
    def increment_checkins(self):
        """
        Return:
            pass
        Effect:
            Increments the number of checkins if the difference between
            the latest ping and the one before it is more than 1 hour
        Author: Shanab
        """
        last_two_pings = Ping_Log.objects.filter(who=self).reverse()[:2]
        # the if statement checks if this is the user's first ping, or if
        # the difference between the latest 2 pings is more than one hour
        if len(last_two_pings) < 2 or last_two_pings[0].time - last_two_pings[1].time >= timedelta(hours=1):
            self.number_of_checkins += 1
            self.save()
        pass
    
    def __unicode__(self):
        return str(self.installation_id)
    
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
    polypoints = models.CharField(max_length=50000)
    start_location = models.ForeignKey(Node, related_name='start')
    end_location = models.ForeignKey(Node, related_name='end')

    def __unicode__(self):
        return str(self.start_location.id) + ", " + str(self.end_location.id)
        
    class Meta:
        ordering = ["id"]

class Step_History(models.Model):
    step = models.ForeignKey(Step, related_name='current_step')
    time = models.DateTimeField()
    speed = models.FloatField()

    def __unicode__(self):
        return str(self.step) + "," + str(self.time)

class Ping_Log(models.Model):
    step = models.ForeignKey(Step)
    speed = models.FloatField() # in m/s
    who = models.ForeignKey(Device)
    time = models.DateTimeField()
    persistence = models.IntegerField(default=1)
    
    def __unicode__(self):
        return str(self.time)
    
    class Meta:
        ordering = ["time"]

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


class Badge(models.Model):
    devices = models.ManyToManyField(Device)
    name = models.CharField(max_length=80)
    value = models.CharField(max_length=80)
    description = models.CharField(max_length=600)
    
    def __unicode__(self):
        string = str(self.name)
        if self.value:
            string += ", " + str(self.value)
        return string
        
    class Meta:
        ordering = ["id"]
    
    
"""
    BUSINESS LOGIC
    IN DJANGO, IT IS ADVISED TO KEEP LOGIC IN THE MODELS
"""

# @author: Moataz Mekki
# takes "from" & "to" locations/addresses, calls Google maps
# gets the routes required, saves them in the database
# @param origin: can be address or long & lat
# @param destination: can be address or long & lat
# @return: list of routes between the origin & the destination
def getdirections(origin, destination):
    url = 'http://maps.googleapis.com/maps/api/directions/json?origin=' + origin + '&destination=' + destination + '&sensor=true&alternatives=true'
    # print "making request to %s" % url
    result = json.load(urllib.urlopen(url))
    routes = result['routes']
    # all_routes = []
    return_routes =[]
    for route in routes :
        summ = route['summary']
        legs = route['legs']
        current_route = Route(summary=summ)
        current_route.save()
        # all_routes.append(current_route)
        for leg in legs :
            distance_text = leg['distance']['text']
            distance_value = leg['distance']['value']
            duration_text = leg['duration']['text']
            duration_value = leg['duration']['value']
            start_address = leg['start_address']
            end_address = leg['end_address']
            start_loc = leg['start_location']
            end_loc = leg['end_location']
            s_node = get_node(latitude=start_loc['lat'],
                              longitude=start_loc['lng'])
            # s_node.save()
            e_node = get_node(latitude=end_loc['lat'],
                            longitude=end_loc['lng'])
            # e_node.save()
            steps = leg['steps']
            current_leg = Leg(duration_text=duration_text,
                              duration_value=duration_value,
                              distance_text=distance_text,
                              distance_value=distance_value,
                              start_address=start_address,
                              end_address=end_address,
                              start_location=s_node,
                              end_location=e_node)
            current_leg.save()
            for step in steps:
                html = step['html_instructions']
                distance_text = step['distance']['text']
                distance_value = step['distance']['value']
                duration_text = step['duration']['text']
                duration_value = step['duration']['value']
                start_location = step['start_location']
                end_location = step['end_location']
                polypoints = step['polyline']['points']
                start_node = get_node(latitude=start_location['lat'],
                                  longitude=start_location['lng'])
                # start_node.save()
                end_node = get_node(latitude=end_location['lat'],
                                longitude=end_location['lng'])
                # end_node.save()
                current_step = get_step(html,
                                     duration_text,
                                     duration_value,
                                     distance_text,
                                     distance_value,
                                     polypoints,
                                     start_node,
                                     end_node)
                current_leg.steps.add(current_step)
            current_leg.save()
            current_route.legs.add(current_leg)
        current_route.save()
        return_routes.append(current_route)
    return return_routes

"""
Returns a node from the latitude and longitude given
@author kamasheto
"""
def get_node(latitude, longitude):
    try:
        node = Node.objects.get(longitude=longitude, latitude=latitude)
    except Node.DoesNotExist:
        node = Node(longitude=longitude, latitude=latitude)
        node.save()
    return node


def get_step(html, duration_text, duration_value,
             distance_text,distance_value, polypoints, start_node,end_node):
    try:
        current_step = Step.objects.get(start_location = start_node, end_location = end_node)
    except Step.DoesNotExist:
        current_step = Step(html_instructions=html,
                            duration_text=duration_text,
                            duration_value=duration_value,
                            distance_text=distance_text,
                            distance_value=distance_value,
                            polypoints = polypoints,
                            start_location=start_node,
                            end_location=end_node)
        current_step.save()
    return current_step
        
#@author: Monayri
#@param myStep: The Step that i am currently at
#@param legID: The id of the leg i am taking
#@param destination: The destination Node
#@return: list of routes
def getalternatives(leg, myStep, destination, location):
    #First i will call the subRoutes Method
    routes = [] 
    if myStep == None:
        routes = getdirections(str(location.latitude)+","+str(location.longitude), str(destination.latitude)+","+str(destination.longitude))
    else:
        routes = compute_subroutes(leg, myStep, destination)
    
    if not routes:
        routes = getdirections(str(myStep.start_location.latitude)+","+str(myStep.start_location.longitude), 
                            str(destination.latitude)+","+str(destination.longitude))
    if leg != None :
        steps =[] 
        duration = 0
        distance = 0
        for step in leg.steps:
            if(step != myStep):
                steps.append(step)
                duration += step.duration_value
                distance += step.distance_value
            else:
                break
        for route in routes:
            for myleg in route.legs:
                summ =""
                current_route=Route(summary=summ)
                current_route.save()
                distance_text = ""
                distance_value = distance + myleg.distance_value
                duration_text = ""
                duration_value = duration + myleg.duration_value
                start_address = leg.start_address
                end_address = myleg.end_address
                start_loc = leg.start_location
                end_loc = myleg.end_location
                current_leg = Leg(duration_text = duration_text, 
                                  duration_value = duration_value, 
                                  distance_text = distance_text, 
                                  distance_value = distance_value, 
                                  start_address = start_address, 
                                  end_address = end_address,
                                  start_location = start_loc, 
                                  end_location = end_loc)
                current_leg.save()
                current_leg.steps.extend(steps)
                current_leg.steps.extend(leg.steps)
                current_leg.save()
                current_route.legs.add(current_leg)
                current_route.save()
#        response +=  '"routes" :[]'

    steps = getDifferences(routes)
    response = {"routes":[]}
    for route in routes :
        r = {"summary" : route.summary, "legs":[]}
        for leg in route.legs.all():
            start_node = Node.objects.get(id=leg.start_location.id)
            end_node = Node.objects.get(id=leg.end_location.id)
            l = {"distance" : {"text":leg.distance_text, 
                               "value": leg.distance_value}, 
                 "end_address": leg.end_address,
                 "end_location": {"lat" : end_node.latitude ,
                                  "lng" : end_node.longitude},
                 "start_address" : leg.start_address,
                 "start_location" : {"lat" : start_node.latitude,
                                     "lng" : start_node.longitude},
                 "steps" : []
                 }
            for step in leg.steps.all():
                start_node2 = Node.objects.get(id=step.start_location.id)
                end_node2 = Node.objects.get(id=step.end_location.id)
                if(steps):
                    for step2 in steps:
                        if (step2.id == step.id):
                            s = {"distance" : {"text": step.distance_text,
                                               "value": step.distance_value},
                                 "duration" : {"text": step.duration_text,
                                               "value": step.duration_value},
                                 "end_location": {"lat": end_node2.latitude,
                                                  "lng": end_node2.longitude},
                                 "start_location": {"lat": end_node2.latitude,
                                                  "lng": end_node2.longitude},
                                  "loc":step.id,
                                  "marker" : 1,
                                  "speed" : get_step_speed(step),
                                  "polyline" : step.polypoints
                                 }
                            break
                        else:
                            s = {"distance" : {"text": step.distance_text,
                                               "value": step.distance_value},
                                 "duration" : {"text": step.duration_text,
                                               "value": step.duration_value},
                                 "end_location": {"lat": end_node2.latitude,
                                                  "lng": end_node2.longitude},
                                 "start_location": {"lat": end_node2.latitude,
                                                  "lng": end_node2.longitude},
                                  "loc":step.id,
                                  "marker" : 0,
                                  "speed" : get_step_speed(step),
                                  "polyline" : step.polypoints
                                 }
                else:
                    s = {"distance" : {"text": step.distance_text,
                                               "value": step.distance_value},
                                 "duration" : {"text": step.duration_text,
                                               "value": step.duration_value},
                                 "end_location": {"lat": end_node2.latitude,
                                                  "lng": end_node2.longitude},
                                 "start_location": {"lat": end_node2.latitude,
                                                  "lng": end_node2.longitude},
                                  "loc":step.id,
                                  "marker" : 0,
                                  "speed" : get_step_speed(step),
                                  "polyline" : step.polypoints
                                 }
                l['steps'].append(s)
            r['legs'].append(l)
        response['routes'].append(r)
    return response


#@Author : Ahmed Abouraya
#Takes a node and radius and returns all steps around this node in a radius radius
#@param lng: longitude of the node
#@param lat: latitude of the node
#@param radius:radius of the region
def get_steps_around(lng, lat,radius):
	steps=Step.objects.all()
	L = list() # empty list
	for s in steps:
		if math.sqrt(pow(abs(s.start_location.longitude-lng),2)+pow(abs(s.start_location.latitude-lat),2))<radius:
			stepHistoryList = Step_History.objects.filter(step__start_location__latitude=s.start_location.latitude,
                                                    step__start_location__longitude=s.start_location.longitude,
                                                    step__end_location__latitude=s.end_location.latitude,
                                                    step__end_location__longitude=s.end_location.longitude)[:5]
                	counter=0
                        avgSpeed=0
                        for a in stepHistoryList.all():
                                counter=counter+1
                                avgSpeed=avgSpeed+a.speed
                        if counter==0:
                               avgSpeed=-1
                        else:                           
                                avgSpeed=avgSpeed/counter
			L.append     ({'start_location_longitude':s.start_location.longitude,'start_location_latitude':s.start_location.latitude,'end_location_longitude':s.end_location.longitude
,'end_location_latitude':s.end_location.latitude,'avg_speed':avgSpeed
})

	return 	json.dumps(L)
             
#@Author : Ahmed Abouraya
# takes a JSONObject and updates all steps speeds with the information in the database
#@param result JSONObject
# Logic added to evaluate ^k
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
#@Author : Ahmed Abouraya
# calculates distance between two nodes
#@param current: first node
#@param target : second node
def getDistance(current,target):
        lat = current['lat'] / 1E6 - target['lat']  / 1E6;
        lng = current['lng']  / 1E6 - target['lng']  / 1E6;
        return math.sqrt(lat*lat+lng*lng)
 

"""
Modified version of evaluate (look below) optimized for integration
@author kamasheto
"""
def evaluate_route(result, speed, my_step):
    # print result
    routes = result['routes']
    alternatives = []
    for route in routes:
            summ = route['summary']
            legs = route['legs']
            for leg in legs:
                    # distance_text = leg['distance']['text']
                    # distance_value = leg['distance']['value']
                    # duration_text = leg['duration']['text']
                    # duration_value = leg['duration']['value']
                    # start_address = leg['start_address']
                    # end_address = leg['end_address']
                    # start_loc = leg['start_location']
                    # end_loc = leg['end_location']
                    steps = leg['steps']
                    # flag=True
                    # #check if speed is 0 insert current step as blocked
                    # if blockedRoad(speed):
                    #         currentStepHistory = Step_History(step = currentStep, time = datetime.now(), speed = 0)
                    #         currentStepHistory.save()

                    for s, step in enumerate(steps):
                        step['speed'] = 1
                        if my_step == step:
                            for i in range(s + 1, len(steps)):
                                f_step = steps[i] # future step, calc speed
                                max_speed = get_step_speed(f_step)
                                # sexier algo maybe?
                                f_step['speed'] = max_speed
                                if blocked_road(max_speed):
                                    dest = steps[-1].end_location
                                    dest = get_node(dest['latitude'], dest['longitude'])
                                    start = get_node(step['start_location']['latitude'], step['start_location']['longitude'])
                                    alternatives.append(getalternatives(leg, f_step, dest, step.start_location))
                            break
    for alt in alternatives:
        for route in alt['routes']:
            for leg in route['legs']:
                for step in leg['steps']:
                    step['speed'] = get_step_speed(Step.objects.get(start_location = get_node(step['start_location']['latitude'], step['start_location']['longitude']),
                                                                    end_location = get_node(step['end_location']['latitude'], step['end_location']['longitude'])))
            routes.append(route)
    return routes

# @Author: Ahmed Abouraya
# checks if the current road is blocked,if so it updates the database
# loops over all steps, when the currentStep is reached, checks whether the driver has reached the end of the step or not if yes insert information in database
# checks for future steps if they're blocked if yes checks for alternatives
# @param origin: start node
# @param destination: destination node
# @param result : JSONObject
# @param speed : current speed
# @param currentStep : current step
# @param startTime : start time of the current step
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
			if blocked_road(speed):
				currentStepHistory = Step_History(step = currentStep,time=datetime.now(),speed=0)
								#fix step=currentStep, a database object and JSON object				
				currentStepHistory.save()

			for step in steps:
				if step==currentStep:
				#if current step is not reached check if the user has reached it's end
					flag=False
					if getDistance(origin,currentStep['end_location'])<0.0002 :
					#	currentStepHistory = Step_History(step = currentStep,time=datetime.now(),
						            
				#fix step=currentStep, a database object and JSON object
						timeDiff=(startTime-datetime.now())
						days = timeDiff.days*24*60
					 	hours = timeDiff.seconds*3600
					  	minutes = timeDiff.seconds*60
					  	seconds = timeDiff.seconds
						spd=(days+hours+minutes+seconds)/currentStep['distance']['value']
						currentStepHistory = Step_History(step__start_location__latitude=currentStep['start_location']['lat'],
						step__start_location__longtitude=currentStep['start_location']['lng'],
						step__end_location__latitude=currentStep['end_location']['lat'],
						step__end_location__longitude=currentStep['end_location']['lng'],time=datetime.now(),speed=spd)
						currentStepHistory.save()
				
				if flag :
				#if currentStep is not reached skip
					continue

				#if currentStep is reached check if a future step is blocked
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
                        if blocked_road(speed):
                                currentStepHistory = Step_History(step = currentStep,time=datetime.now(),speed=0)
                                currentStepHistory.save()

                        for step in steps:
                                if step==currentStep:
                                #if current step is not reached check if the user has reached it's end
                                        flag=False
                                        if getDistance(origin,currentStep['end_location'])<0.0002 :
                                                currentStepHistory = Step_History(step = currentStep,time=datetime.now(),
                                                                            speed=(startTime-datetime.now())/currentStep['distance']['value'])
                                        currentStepHistory.save()
                                
                                # if flag :
                                # #if currentStep is not reached skip
                                #         continue

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
					if blocked_road(s.speed):
						counter=counter+1
				if counter>0:
				#request for alternatives
					return updateResult(getalternatives(leg, step, destination, origin))
		return updateResult(result)

#@author: Ahmed Abouraya
#@param  speed: current speed
#determines whether a road is blocked or not
def blocked_road(speed):
	return speed < 5

# @author:  Shanab
# @param    leg:            current leg that the user is moving in (can be None)
# @param    step:           current step that the user is moving in
# @param    destination:    destination node for the user
# The method tries to find the possible subroutes that can go from
# the end node of the provided step to the provided destination.
# Also the method saves the newly found subroute.
# If there wasn't any subroutes found, the method will return *[]*!
# If destination wasn't provided the method tries to find subroutes
# that can go from the end node of the provided step to the end node
# of the provided leg
def compute_subroutes(leg, step, destination):
    start_node = step.end_location
    end_node = destination
    legs = list(Leg.objects.filter(steps__start_location = start_node).filter(steps__end_location = end_node))
    # If one of the filtered legs happens to be the input leg, then remove it
    try:
        legs.remove(leg)
    except:
        pass
    print "Found " + str(len(legs)) + " leg(s)!"
    if len(legs) != 0:
        return find_and_create_subroute(legs, start_node, end_node)
    else:
        return []
    pass

# @author:  Shanab
# @param    legs:       All the legs that have a path that will lead
#                       from start_node to end_node
# @param    start_node: the start node of the subroute
# @param    end_node:   the end node of the subroute
# The method finds the subroute that will lead from the provided
# start node to the provided end node in the input leg
def find_and_create_subroute(legs, start_node, end_node):
    result_routes = []
    for leg in legs:
        steps = ordered_steps(leg)
        print "Steps: " + str(steps)
        i = 0
        for step in steps:
            if step.start_location == start_node:
                start_index = i
                break
            i += 1
        for step in steps[start_index:]:
            if step.end_location == end_node:
                end_index = i
                break
            i += 1
        end_index += 1
        print "Start index:\t" + str(start_index)
        print "End index:\t" + str(end_index)
        subroute_steps = steps[start_index:end_index]
        print "Subroute Steps: " + str(subroute_steps)
        if len(subroute_steps) != len(steps):       # if the subroute length was equal to the route length
                                                    # this means the route doesn't need to be saved
            new_leg =   Leg(duration_value  = sum_duration_values(subroute_steps),
                            distance_value  = sum_distance_values(subroute_steps),
                            start_location  = start_node,
                            end_location    = end_node)
            new_leg.save()                
            new_leg.steps = subroute_steps
            new_leg.save()
            new_route = Route()
            new_route.save()
            new_route.legs.add(new_leg)
            new_route.save()
            result_routes.append(new_route)
        else:
            route = leg.route_set.all()[0]
            result_routes.append(route)
    return result_routes

# @author:  Shanab
# @param:   x
# @param:   y
# returns the addition of two numbers x and y
def add(x,y): return x + y

# @author:  Shanab
# @param    step
# returns the distance value of the provided step
def get_distance_value(step): return step.distance_value

# @author:  Shanab
# @param    step
# returns the duration value of the provided step
def get_duration_value(step): return step.duration_value

# @author:      Shanab
# @param steps: a list of steps
# returns the summation of distance values for the provided list of steps
def sum_distance_values(steps):
    return reduce(add, map(get_distance_value, steps))

# @author:      Shanab
# @param        steps: a list of steps
# returns the summation of duration values for the provided list of steps
def sum_duration_values(steps):
    return reduce(add, map(get_duration_value, steps))

        
# @author:      Shanab
# @param        leg
# returns an ordered list of steps where the end node of a step
# is the start node of the next step
def ordered_steps(leg):
    result = []
    steps = list(leg.steps.all())
    node = leg.start_location
    end_node = leg.end_location
    while True:
        temp_step = [step for step in steps if step.start_location == node][0]
        result.append(temp_step)
        node = temp_step.end_location
        if node == end_node:
            break
    return result


"""
Gets step from a given node
@author kamasheto
"""
def get_step_from_node(n):
    # lets pretend we can get a step by checking if a node is between its start and end points
    # this obviously has flaws but lets see how it goes from there
    for step in Step.objects.all():
        # check if this node can lie in this step
        s, e = step.start_location, step.end_location
        if ((s.latitude <= n.latitude and n.latitude <= e.latitude and s.longitude <= n.longitude and n.longitude <= e.longitude) or
            (s.latitude >= n.latitude and n.latitude >= e.latitude and s.longitude <= n.longitude and n.longitude <= e.longitude) or
            (s.latitude >= n.latitude and n.latitude >= e.latitude and s.longitude >= n.longitude and n.longitude >= e.longitude) or
            (s.latitude <= n.latitude and n.latitude <= e.latitude and s.longitude >= n.longitude and n.longitude >= e.longitude)):
            return step
    return None # couldn't find a step
    
def get_step_speed(step):
    logs = list(Ping_Log.objects.filter(step = step, time__gte=datetime.now() - timedelta(seconds = 10)))
    # if we have a moving car, use its speed as the actual speed!
    max_speed = -1 # remember to use -1 as a check for no-one there, empty street maybe?
    for log in logs:
        max_speed = max(max_speed, log.speed)
    return max_speed
    
def get_device(who):
    try:
        d = Device.objects.get(installation_id = who)
    except:
        # register device for the first time
        d = Device(installation_id = who)
        d.save()
    return d
    
def get_color_from_speed(speed):
    if speed == -1:
        return 0xff0000ff # blue
    elif speed <= 5:
        return 0xff000000 # black
    elif speed <= 10:
        return 0xffff0000 # red
    elif speed <= 15:
        return 0xffff8000  # orange
    elif speed <= 20:
        return 0xffffff00 # yellow
    else:
        return 0xff00ff00 # green


""""
This Method checks if a step is blocked according to speed
@param step: the object step to be checked
@return: Boolean indicating if the step is blocked or not
@author Monayri
"""
def ifStepBlocked(step):
    speed = get_step_speed(step)
    if speed == -1 :
        return False 
    else:
        if speed <= 5:
            return True
        else:
            return False

""""
This Method gets the steps of a route
@param route: the route object that its steps are needed
@return: a list of steps
@author Monayri
"""  
def getRouteSteps(route):
    myLeg = route.legs[0]
    steps = myLeg.steps
    return steps      


""""
This Method checks if a route is blocked according to speed
@param step: the route object to be checked
@return: Boolean indicating if the route is blocked or not
@author Monayri
"""
def ifRouteBlocked(route):
    steps = getRouteSteps(route)
    for step in steps:
        if ifStepBlocked(step):
            return True


""""
This Method gets the differences between routes and returns the steps that represents 
the differences between routes
@param rotues : the routes suggested to the user
@return: list of difference steps (max.3)
@author Monayri
"""
def getDifferences(routes):
    step1 = None
    step2 = None
    step3 = None
    steps = []
    if len(routes) == 2 :
        steps1 = []
        steps2 = []
        for leg in routes[0].legs.all():
            for step in leg.steps.all():
                steps1.append(step)
        for leg in routes[1].legs.all():
            for step in leg.steps.all():
                steps2.append(step)
        x = 0
        while x < len(steps1):
            if steps1[x].id != steps2[x].id:
                step1 = steps1[x]
                step2 = steps2[x]
                steps.append(step1)
                steps.append(step2)
                break
            x += 1
    
    if len(routes) == 3 :
        steps1 = []
        steps2 = []
        steps3 = []

        for leg in routes[0].legs.all():
            for step in leg.steps.all():
                steps1.append(step)
        for leg in routes[1].legs.all():
            for step in leg.steps.all():
                steps2.append(step)
        for leg in routes[2].legs.all():
            for step in leg.steps.all():
                steps3.append(step)
        x = 0
        while x < len(steps1):
            if steps1[x].id != steps2[x].id and steps1[x].id != steps3[x].id:
                if step1 == None:
                    step1 = steps1[x]
                    steps.append(step1)
            if steps2[x].id != steps1[x].id and steps2[x].id != steps3[x].id:
                if step2 == None:
                    step2 = steps2[x]
                    steps.append(step2)
            if steps3[x].id != steps1[x].id and steps3[x].id != steps2[x].id:
                if step3 == None:
                    step3 = steps3[x]
                    steps.append(step3)
            if len(steps) == 3:
                break
            x += 1
    return steps
  
################## START OF BADGE HANDLERS ##################
def badge_handler(who, speed):
    """
    Return:
        a list of badges that the user has acquired
    Arguments:
        who: Device object
        speed: The speed of the user
    Author: Shanab
    """
    badges = []
    speed_badge = speed_badge_handler(who, speed)
    if speed_badge:
        badges.append(speed_badge)
    checkin_badge = checkin_badge_handler(who)
    if checkin_badge:
        badges.append(checkin_badge)
    time_badge = time_badge_handler(who)
    if time_badge:
        badges.append(time_badge)
    badger_badge = badger_badge_handler(who)
    if badger_badge:
        badges.append(badger_badge)
    persistent_time_badge = persistent_time_badge_handler(who)
    if persistent_time_badge_handler:
        badges.append(persistent_time_badge)
    persistent_time_and_speed_badge = persistent_time_and_speed_badge_handler(who)
    if persistent_time_and_speed_badge_handler:
        badges.append(persistent_time_and_speed_badge_handler)
    return badges
    
    
def speed_badge_handler(who, speed):
    """
    Return:
        a badge if the user reached a speed that acquires this badge,
        and if he/she hasn't already acquired this badge.
        Returns None if the user hasn't acquired any speedster badges,
        or if he acquired one previously.
    Arguments:
        who: Device object
        speed: The speed of the user in mps
    Author: Shanab
    """
    speed = to_kph(speed)
    badge = None
    if speed >= 180:
        badge = Badge.objects.get(name="speedster", value=180)
    elif speed >= 140:
        badge = Badge.objects.get(name="speedster", value=140)
    elif speed >= 100:
        badge = Badge.objects.get(name="speedster", value=100)
        
    if badge and not who.has_badge(badge):
        who.badge_set.add(badge)
        
    return badge
    
    
def checkin_badge_handler(who):
    """
    Return:
        a badge if this is either the first time, or the
        [50,100,500,1000,...]th time the user uses the application.
        Returns None if the user hasn't acquired any checkin badges,
        or if he already acquired it previously.
    Arguments:
        who: Device object
    Author: Shanab
    """
    badge = None
    if who.number_of_checkins in checkin_badge_values():
        badge = Badge.objects.get(name="checkin",value=who.number_of_checkins)
        who.badge_set.add(badge)
    return badge


def badger_badge_handler(who):
    """
    Return:
        The "badger" badge if the user acquired all set of badges
        (except for the badger badge of course), else returns None.
    Arguments:
        who: Device object
    Author: Shanab
    """
    badge = None
    if who.badge_set.count() == Badge.objects.count() - 1:
        badge = Badge.objects.get(name="badger")
        who.badge_set.add(badge)
    return badge
    
        
def time_badge_handler(who):
    """
    Return:
        Either one of [Adventurer, Addict, Fanboy, Super User] badges
        if the user used the application for [10 days in a month,
        10 consecutive days, 30 consecutive days, 60 consecutive days] respectively;
        Or None otherwise or if the user already acquired the badge previously.
    Arguments:
        who: Device object
    Author: Shanab
    """
    badge = None
    adventurer_badge = Badge.objects.get(name="adventurer")
    addict_badge = Badge.objects.get(name="addict")
    fanboy_badge = Badge.objects.get(name="fanboy")
    super_user_badge = Badge.objects.get(name="super-user")
    usage_dates = Ping_Log.objects.filter(who=who).dates('time', 'day').reverse()
    badges = who.badge_set.all()
    # For the user to acquire any of the listed badges
    # He has to have used the application for more than 10 days
    if len(usage_dates) >= 10:
        if not adventurer_badge in badges:
            end_date = usage_dates[0] - timedelta(days=30)
            usage_dates_in_past_30_days = filter(lambda i: i >= end_date, usage_dates)
            if len(usage_dates_in_past_30_days) >= 10:
                who.badge_set.add(adventurer_badge)
                return adventurer_badge
        elif not addict_badge in badges:
            badge = consecutive_time_badge_handler(who, addict_badge, 10, usage_dates)
        elif not fanboy_badge in badges:
            badge = consecutive_time_badge_handler(who, fanboy_badge, 30, usage_dates)
        elif not super_user_badge in badges:
            badge = consecutive_time_badge_handler(who, super_user_badge, 60, usage_dates)
    return badge
    

def persistent_time_badge_handler(who):
    """
    Return:
        Either one of [Road Warrior, Wheel Junkie] badges
        if the user was reported using the application continuously
        for [3, 5] hours respectively; Or None otherwise or if the
        user acquired this badge previously.
    Arguments:
        who: Device object
    Author: Shanab
    """
    badge = None
    warrior_badge = Badge.objects.get(name="road-warrior")
    junkie_badge = Badge.objects.get(name="wheel-junkie")
    if len(Ping_Log.objects.filter(who=who)) >= 2:
        last_ping = Ping_Log.objects.filter(who=who).reverse()[0]
        trip = Ping_Log.objects.filter(who=who, persistence=last_ping.persistence)
        start_time_of_trip = trip[0].time
        end_time_of_trip = trip.reverse()[0].time
        if end_time_of_trip - start_time_of_trip >= timedelta(hours=3) and not warrior_badge in who.badge_set.all():
            badge = warrior_badge
            who.badge_set.add(badge)
        elif end_time_of_trip - start_time_of_trip >= timedelta(hours=5):
            badge = junkie_badge
            who.badge_set.add(badge)
    return badge


def persistent_time_and_speed_badge_handler(who):
    """
    Return:
        Either one of [Turtle Speed, Grandma, Snail Like]
        badges if the user was reported driving at an average speed
        <= [10, 5, 2] kph respectively for +30 minutes;
        Or Lunatic badge if the user was reported driving at
        an average speed >= 140 kph for +20 minutes;
        Or Wacko badge if the user was reported driving at
        an average speed >= 180 for +10 minutes;
        Or None otherwise or if the user acquired this badge previously
    Arguments:
        who: Device object
        speed: The speed of the user in mps
    Author: Shanab
    """
    badge = None
    if len(Ping_Log.objects.filter(who=who)) >= 2:
        last_ping = Ping_Log.objects.filter(who=who).reverse()[0]
        trip = Ping_Log.objects.filter(who=who, persistence=last_ping.persistence)
        start_time_of_trip = trip[0].time
        end_time_of_trip = trip.reverse()[0].time

        badges = Badge.objects.all()
        wacko_badge = badges[18]
        lunatic_badge = badges[17]
        snail_badge = badges[16]
        grandma_badge = badges[15]
        turtle_badge = badges[14]
        device_badges = who.badge_set.all()
        average_trip_speed = to_kph(sum(trip.values_list('speed', flat=True)) / len(trip))
        if average_trip_speed >= 180 and end_time_of_trip - start_time_of_trip >= timedelta(minutes=10):
            if wacko_badge not in device_badges:
                badge = wacko_badge
            elif lunatic_badge not in device_badges:
                badge = lunatic_badge
        elif average_trip_speed >= 140 and end_time_of_trip - start_time_of_trip >= timedelta(minutes=20):
            badge = lunatic_badge if not lunatic_badge in device_badges else None
        elif average_trip_speed <= 10 and average_trip_speed >= 5 and end_time_of_trip - start_time_of_trip >= timedelta(minutes=20):
            badge = turtle_badge if not turtle_badge in device_badges else None
        elif average_trip_speed <= 5 and average_trip_speed >= 2 and end_time_of_trip - start_time_of_trip >= timedelta(minutes=20):
            if grandma_badge not in device_badges:
                badge = grandma_badge
            elif turtle_badge not in device_badges:
                badge = turtle_badge
        elif average_trip_speed <= 2 and end_time_of_trip - start_time_of_trip >= timedelta(minutes=20):
            if snail_badge not in device_badges:
                badge = snail_badge
            elif grandma_badge not in device_badges:
                badge = grandma_badge
            elif turtle_badge not in device_badges:
                badge = turtle_badge

    if badge:
        who.badge_set.add(badge)
    return badge

################### Badge Helpers ################
def to_kph(speed_in_mps):
    """Convert from meters per second to kilometers per hour"""
    return speed_in_mps * 60 * 60 / 1000.0
    
    
def checkin_badge_values():
    """
    Return:
        a list of integers where each number represents
        the number of checkins that the user must get
        in order to acquire a "checkin" badge.
    Author: Shanab
    """
    return map(lambda x:int(x.value), Badge.objects.filter(name="checkin"))

def consecutive_time_badge_handler(who, badge, consecutive_days, usage_dates):
    """
    Return: Input badge if the user used the application for {consecutive_days} consecutive days,
            None otherwise
    Arguments:
        who: Device object
        badge: Badge object
        consecutive_days: Number of consecutive days that the user must've
                          used the application in order to acquire the input badge
        usage_dates: An array containing the dates of days that the user used the application in
    Effect:
        Creates a relation between the user "who" and the badge "badge"
        if the user acquired this badge
    Author: Shanab
    """
    end_date = usage_dates[0] - timedelta(consecutive_days)
    filtered_usage_dates = filter(lambda i: i >= end_date, usage_dates)
    if len(filtered_usage_dates) >= consecutive_days:
        who.badge_set.add(badge)
        return badge
    else:
        return None

def get_persistence(who, time=datetime.now()):
    pings = Ping_Log.objects.filter(who=who).reverse()
    # persistence = pings[0].persistence + 1 if len(pings) != 0 and datetime.now() - pings[0].time >= timedelta(hours=1) else pings[0].persistence
    if len(pings) != 0:
        if time - pings[0].time >= timedelta(minutes=5, seconds=30):
            persistence = pings[0].persistence + 1
        else:
            persistence = pings[0].persistence
    else:
        persistence = 1
    return persistence
################### END OF BADGE HANDLERS ###################
