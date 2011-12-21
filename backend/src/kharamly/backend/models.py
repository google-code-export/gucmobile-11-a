from datetime import *
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

class User_loginInfo(models.Model):
    twitterUsername = models.CharField(max_length=200)
    token = models.CharField(max_length=200)
    secret = models.CharField(max_length=200)


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
    persistence = models.IntegerField()
    
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
                                     start_node,
                                     end_node)
                current_leg.steps.add(current_step)
            current_leg.save()
            current_route.legs.add(current_leg)
        current_route.save()
    return result

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
             distance_text,distance_value, start_node,end_node):
    try:
        current_step = Step.objects.get(start_location = start_node, end_location = end_node)
    except Step.DoesNotExist:
        current_step = Step(html_instructions=html,
                            duration_text=duration_text,
                            duration_value=duration_value,
                            distance_text=distance_text,
                            distance_value=distance_value,
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
    if myStep == None:
        return getdirections(str(location.latitude)+","+str(location.longitude), str(destination.latitude)+","+str(destination.longitude))
    routes = compute_subroutes(leg, myStep, destination)
    if not routes:
        return getdirections(str(myStep.start_location.latitude)+","+str(myStep.start_location.longitude), 
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

    
    response = {"routes":[]}
    for route in routes :
        r = [{"summary" : route.summary, "legs":[]}]
        for leg in route.legs:
            start_node = Node.objects.get(id=leg.start_location.id)
            end_node = Node.objects.get(id=leg.end_location.id)
            l = [{"distance" : {"text":leg.distance_text, 
                               "value": leg.distance_value}, 
                 "end_address": leg.end_address,
                 "end_location": {"lat" : end_node.latitude ,
                                  "lng" : end_node.longitude},
                 "start_address" : leg.start_address,
                 "start_location" : {"lat" : start_node.latitude,
                                     "lng" : start_node.longitude},
                 "steps" : []
                 }]
            for step in leg.steps:
                start_node2 = Node.objects.get(id=step.start_location.id)
                end_node2 = Node.objects.get(id=step.end_location.id)
                s = [{"distance" : {"text": step.distance_text,
                                   "value": step.distance_value},
                     "duration" : {"text": step.duration_text,
                                   "value": step.duration_value},
                     "end_location": {"lat": end_node2.latitude,
                                      "lng": end_node2.longitude},
                     "start_location": {"lat": end_node2.latitude,
                                      "lng": end_node2.longitude}
                     }]
                l["steps"] += s
            r["legs"] +=l
        response["routes"]+=r             
    return response




def getLoginInfo(userName):	
	userInfo = User_loginInfo.objects.filter(twitterUsername=userName)
	for s in userInfo.all():
		return { 'token':s.token, 'secret':s.secret } 


def saveTwitterUserInfo(userName,tok,sec):
	userInfo = User_loginInfo(twitterUserName=userName,token=tok,secret=sec)
    	userInfo.save()
 

def checkUserExists(userName):
	userInfo = User_loginInfo.objects.filter(twitterUsername=userName)
	for s in userInfo.all():
		return True
	return False
              
#def getNodesAround(lat,lng):
#	userInfo = User_loginInfo.objects.filter(twitterUserName=userName,token=tok,secret=sec)
#    	userInfo.save()
#	node=
#class Node(models.Model):
#    latitude = models.FloatField()
#    longitude = models.FloatField()

 
                
# Author : Ahmed Abouraya
# takes a JSONObject and updates all steps speeds with the information in the database
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

# calculates distance between two nodes
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

# checks if the current road is blocked,if so it updates the database
# loops over all steps, when the currentStep is reached, checks whether the driver has reached the end of the step or not if yes insert information in database
# checks for future steps if they're blocked if yes checks for alternatives
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
            return True

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

        
def to_kph(speed_in_mps):
    """Convert from meters per second to kilometers per hour"""
    return speed_in_mps * 60 * 60 / 1000.0
    
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
    return badges
    
def speed_badge_handler(who, speed):
    """
    Return:
        a badge if the user reached a speed that acquires this badge,
        and if he/she hasn't already acquired this badge
    Arguments:
        who: Device object
        speed: The speed of the user
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
    
def checkin_badge_values():
    """
    Return:
        a list of integers where each number represents
        the number of checkins that the user must get
        in order to acquire a "checkin" badge.
    """
    return map(lambda x:int(x.value), Badge.objects.filter(name="checkin"))
    
def checkin_badge_handler(who):
    """
    Return:
        a badge if this is either the first time,
        or the [50,100,500,1000,...]th time the user uses the application
    Arguments:
        who: Device object
    Author: Shanab
    """
    badge = None
    if who.number_of_checkins in checkin_badge_values():
        badge = Badge.objects.get(name="checkin",value=who.number_of_checkins)
        who.badge_set.add(badge)
    return badge

