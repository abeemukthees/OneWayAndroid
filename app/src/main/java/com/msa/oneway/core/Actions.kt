package com.msa.oneway.core


/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

interface Action

interface EventAction : Action

interface NavigateAction : Action

interface AnalyticsAction : Action

object EventConsumedAction : EventAction

object NavigateConsumedAction : NavigateAction