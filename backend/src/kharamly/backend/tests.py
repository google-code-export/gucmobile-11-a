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
        Tests that the user acquires at most three non-repeated badges,
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
        """
        self.make_user_use_application(for_in_days=10, until=29)
        badge = time_badge_handler(self.device)
        self.assertEqual(badge, Badge.objects.get(name="adventurer"))
        self.assertIn(badge, self.device.badge_set.all())

    def test_adventurer_badge_handler_doesnt_return_badge(self):
        """
        Makes sure that adventurer badge handler does not return the badge
        if the user did not use the application for 10 days in a duration
        of 30 days
        """
        self.make_user_use_application(for_in_days=9, until=29)
        badge = time_badge_handler(self.device)
        self.assertIsNone(badge)
        self.assertNotIn(Badge.objects.get(name="adventurer"), self.device.badge_set.all())
    
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

    def make_user_use_application(self, for_in_days, until):
        """
        Effect:
            saves data in Ping_Log in a way that makes the user use
            the application for the given number of days in the past
            given number of days (declared as "until")
        """
        s = set([])
        while len(s) != for_in_days:
            day = randint(1,until)
            if not day in s:
                s.add(day)
                Ping_Log(step_id=1,
                         speed = randint(1,140),
                         who = self.device,
                         time = datetime.now() - timedelta(days=day),
                         persistence = randint(1,10)).save()
        pass

###################### END OF BADGE TESTS ######################