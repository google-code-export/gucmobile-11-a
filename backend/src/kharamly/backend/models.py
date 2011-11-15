from django.db import models

# Create your models here.

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
    
class Route(models.Model):
    summary = models.CharField(max_length=200)
    legs = models.ManyToManyField(Leg)