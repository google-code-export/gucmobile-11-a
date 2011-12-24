"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.

Possible Assertion Methods:

assertEqual(a, b)   a == b   
assertNotEqual(a, b)    a != b   
assertTrue(x)   bool(x) is True  
assertFalse(x)  bool(x) is False     
assertIs(a, b)  a is b  
assertIsNot(a, b)   a is not b  
assertIsNone(x) x is None   
assertIsNotNone(x)  x is not None   
assertIn(a, b)  a in b  
assertNotIn(a, b)   a not in b  
assertIsInstance(a, b)  isinstance(a, b)    
assertNotIsInstance(a, b)   not isinstance(a, b) 

assertRaises(exc, fun, *args, **kwds)   fun(*args, **kwds) raises exc    
assertRaisesRegexp(exc, re, fun, *args, **kwds) fun(*args, **kwds) raises exc and the message matches re 

assertAlmostEqual(a, b) round(a-b, 7) == 0   
assertNotAlmostEqual(a, b)  round(a-b, 7) != 0   
assertGreater(a, b) a > b   
assertGreaterEqual(a, b)    a >= b  
assertLess(a, b)    a < b   
assertLessEqual(a, b)   a <= b  
assertRegexpMatches(s, re)  regex.search(s) 
assertNotRegexpMatches(s, re)   not regex.search(s) 
assertItemsEqual(a, b)  sorted(a) == sorted(b) and works with unhashable objs   
assertDictContainsSubset(a, b)  all the key/value pairs in a exist in b 

assertMultiLineEqual(a, b)  strings 
assertSequenceEqual(a, b)   sequences   
assertListEqual(a, b)   lists   
assertTupleEqual(a, b)  tuples  
assertSetEqual(a, b)    sets or frozensets  
assertDictEqual(a, b)   dicts   
"""

from django.test import TestCase
from backend.models import *
from random import *

######################### BADGE TESTS #########################
class BadgeTest(TestCase):
    fixtures = ['devices']

    def setUp(self):
        self.device = Device.objects.get(id=1)


    def test_speed_badge_handler_returns_badge(self):
        """
        Tests if the speed_badge_handler method returns the expected badge
        Author: Shanab
        """
        for s in xrange(100,181,40):
            speed = self.to_mps(s)
            badge = speed_badge_handler(self.device, speed)
            self.assertEqual(badge, Badge.objects.get(name="speedster", value=s))

    def test_speed_badge_handler_saves_badge(self):
        """
        Tests if the speed_badge_handler method saves the relation between badge
        and user in the join table
        Author: Shanab
        """
        for s in xrange(100,181,40):
            speed = self.to_mps(s)
            speed_badge_handler(self.device, speed)
            self.assertIn(Badge.objects.get(name="speedster", value=s), self.device.badge_set.all())

    def test_speed_badge_handler_saves_at_most_three_badges(self):
        """
        Tests that the user acquires at most three non-repeated speedster badges,
        regardless of how many times the user exceeds the badge speed requirement
        Author: Shanab
        """
        for i in xrange(0,11):
            speed = randint(120,240)
            speed_badge_handler(self.device, speed)
            self.assertLessEqual(self.device.badge_set.count(), 3)

    def test_badger_badge_handler_returns_and_saves_badge(self):
        """
        Tests if the badger_badge_handler returns the badger badge
        and saves it in the database when the user acquires all other
        badges
        Author: Shanab
        """
        all_badges = Badge.objects.all()
        for i in xrange(1,20):
            self.device.badge_set.add(all_badges[i])
        badge = badger_badge_handler(self.device)
        self.assertEqual(badge, Badge.objects.get(id=20))
        self.assertIn(Badge.objects.get(name="badger"), self.device.badge_set.all())

    def test_adventurer_badge_handler_returns_and_saves_badge(self):
        """
        Tests if the adventurer badge is acquired by the user if he
        used the application for 10 days in a period of 30 days
        Author: Shanab
        """
        self.make_user_use_application(for_in_days=10, until=29, using=self.device)
        badge = time_badge_handler(self.device)
        self.assertEqual(badge, Badge.objects.get(name="adventurer"))
        self.assertIn(badge, self.device.badge_set.all())

    def test_adventurer_badge_handler_doesnt_return_badge(self):
        """
        Makes sure that adventurer badge handler does not return the badge
        if the user did not use the application for 10 days in a duration
        of 30 days
        Author: Shanab
        """
        self.make_user_use_application(for_in_days=9, until=29, using=self.device)
        badge = time_badge_handler(self.device)
        self.assertIsNone(badge)
        self.assertNotIn(Badge.objects.get(name="adventurer"), self.device.badge_set.all())

    def test_addict_badge_handler_return_and_saves_badge(self):
        """
        Tests if the addict badge is acquired by the user if he
        used the application 10 consecutive days
        Author: Shanab
        """
        self.make_user_use_application_consecutively(for_in_days=10, using=self.device)
        # Forcing the user to acquire the adventurer badge,
        # in order to be able to acquire further badge levels
        time_badge_handler(self.device)
        badge = time_badge_handler(self.device)
        self.assertEqual(badge, Badge.objects.get(name="addict"))
        self.assertIn(badge, self.device.badge_set.all())

    def test_fanboy_badge_handler_return_and_save_badge(self):
        """
        Tests if the fanboy badge is acquired by the user if he
        used the application 30 consecutive days
        Author: Shanab
        """
        self.make_user_use_application_consecutively(for_in_days=30, using=self.device)
        for i in xrange(1,3):
            time_badge_handler(self.device)
        badge = time_badge_handler(self.device)
        self.assertEqual(badge, Badge.objects.get(name="fanboy"))
        self.assertIn(badge, self.device.badge_set.all())

    def test_super_user_badge_handler_return_and_save_badge(self):
        """
        Tests if the super user badge is acquired by the user if he
        used the application 60 consecutive days
        Author: Shanab
        """
        self.make_user_use_application_consecutively(for_in_days=60, using=self.device)
        for i in xrange(1,4):
            time_badge_handler(self.device)
        badge = time_badge_handler(self.device)
        self.assertEqual(badge, Badge.objects.get(name="super-user"))
        self.assertIn(badge, self.device.badge_set.all())

    def test_time_badge_handler_saves_at_most_four_badges(self):
        """
        Tests that the user acquires at most four non-repeated time badges,
        regardless of how many more days he uses the application
        Author: Shanab
        """
        self.make_user_use_application_consecutively(for_in_days=140, using=self.device)
        for i in xrange(1,randint(5,15)):
            time_badge_handler(self.device)
        self.assertEqual(self.device.badge_set.count(), 4)

    def test_checkin_badge_handler_return_and_save_badges(self):
        """
        Test if checkin_badge_handler returns and saves the proper badge
        in the join table between device and badge
        """
        date = datetime.now() - timedelta(days=50)
        values = [1,50,100] # Cannot test further
        for i in xrange(0,len(values)):
            num_checkins = values[i] - values[i-1] if i != 0 else 1
            for j in xrange(0,num_checkins):
                self.make_user_checkin(date, self.device)
                self.device.increment_checkins()
                date += timedelta(minutes=61)
            badge = checkin_badge_handler(self.device)
            self.assertEqual(badge, Badge.objects.get(name="checkin", value=str(values[i])))
            self.assertIn(badge, self.device.badge_set.all())


    def test_persistent_time_badge_handler_return_and_save_badges(self):
        self.make_user_persistently_move(start_date=datetime.now() - timedelta(days=5),
                                        delta=timedelta(hours=3),
                                        using=self.device)

        badge = persistent_time_badge_handler(self.device)
        self.assertIsNotNone(badge)
        self.assertEqual(badge, Badge.objects.get(name="road-warrior"))
        self.assertIn(badge, self.device.badge_set.all())

        self.make_user_persistently_move(start_date=datetime.now() - timedelta(days=4),
                                        delta=timedelta(hours=6),
                                        using=self.device)
        
        badge = persistent_time_badge_handler(self.device)
        self.assertIsNotNone(badge)
        self.assertEqual(badge, Badge.objects.get(name="wheel-junkie"))
        self.assertIn(badge, self.device.badge_set.all())



    ######################### TEST HELPERS #########################

    def to_mps(self, speed_in_kph):
        """
        Return:
            Speed in meters per second
        Arguments:
            speed_in_kph: speed in kilometers per hour
        Author: Shanab
        """
        return math.ceil(speed_in_kph * 1000 / 60 / 60.0)

    def random_date(self, start, end):
        """
        Return:
            a random datetime object between start and end datetime objects
        Arguments:
            start:  datetime Object
            end:    datetime Object
        Author: Shanab
        """
        delta = end - start
        int_delta = (delta.days * 24 * 60 * 60) + delta.seconds
        random_second = randrange(int_delta)
        return (start + timedelta(seconds=random_second))

    def make_user_use_application(self, for_in_days, until, using):
        """
        Effect:
            saves data in Ping_Log in a way that simulates the user using
            the application for the given number of days in the past
            given number of days (declared as "until")
        Author: Shanab
        """
        s = set([])
        while len(s) != for_in_days:
            day = randint(1,until)
            if not day in s:
                s.add(day)
                Ping_Log(step_id=1,
                        speed = randint(1,140),
                        who = using,
                        time = datetime.now() - timedelta(days=day),
                        persistence = randint(1,10)).save()

    def make_user_use_application_consecutively(self, for_in_days, using):
        """
        Effect:
            saves data in Ping_Log in a way that simulates the user using
            the application for {for_in_days} consecutive days  
        Author: Shanab
        """
        usage_date = datetime.now() - timedelta(days = for_in_days)
        for i in xrange(0,for_in_days):
            Ping_Log(step_id=1,
                    speed = randint(1,140),
                    who = using,
                    time = usage_date,
                    persistence = randint(1,10)).save()
            usage_date += timedelta(days=1)

    def make_user_checkin(self, time, using):
        """
        Simulates the input device {using} checking in at a specified time
        Author: Shanab
        """
        Ping_Log(step_id=1,
                speed = randint(1,140),
                who = using,
                time = time,
                persistence = get_persistence(using)).save()

    def make_user_persistently_move(self, start_date, delta, speed=50, using=Device.objects.get(id=1)):
        """
        Simulates the input user {using} persistently using the application for
        the specified timedelta
        """
        time = start_date
        end_time = time + delta
        while time <= end_time:
            Ping_Log(step_id=1,
                speed = speed,
                who = using,
                time = time,
                persistence = get_persistence(who=using, time=time)).save()
            time += timedelta(minutes=4)
        

###################### END OF BADGE TESTS ######################